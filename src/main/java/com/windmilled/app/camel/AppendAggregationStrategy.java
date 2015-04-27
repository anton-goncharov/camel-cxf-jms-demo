package com.windmilled.app.camel;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Appends addition response to origin message body.
 * It is used within enrich operation.
 */
public class AppendAggregationStrategy implements AggregationStrategy {

    public static final String FIELD_TO_AGGREGATE = "items";
    public static final String FIELD_TYPE = "type";
    public static final String COLLECTION = "collection";

    private ObjectMapper mapper = new ObjectMapper();
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Override
	public Exchange aggregate(Exchange origin, Exchange addition) {
        try {
            StringBuilder originalData = new StringBuilder((String) origin.getIn().getBody());
            StringBuilder additionData = new StringBuilder((String) addition.getIn().getBody());

            // merge collections from both messages
            String merged = mergeData(originalData.toString(), additionData.toString());

            if (origin.getPattern().isOutCapable()) {
                origin.getOut().setBody(merged);
            } else {
                origin.getIn().setBody(merged);
            }
            return origin;
        } catch (Exception e) {
            logger.error("Error merging origin and addition data", e);
            throw new RuntimeException(e);
        }
    }

    private String mergeData(String origin, String addition) throws Exception {
        // parse messages
        JsonNode originJson = mapper.readTree(origin);
        JsonNode additionJson = mapper.readTree(addition);
        // check message type
        if (originJson.has(FIELD_TYPE) && additionJson.has(FIELD_TYPE)
                && originJson.get(FIELD_TYPE).textValue().equalsIgnoreCase(COLLECTION)
                && additionJson.get(FIELD_TYPE).textValue().equalsIgnoreCase(COLLECTION)) {
            JsonNode additionItems = null;
            JsonNode originItems = null;
            if (originJson.has(FIELD_TO_AGGREGATE)) {
                originItems = originJson.get(FIELD_TO_AGGREGATE);
            }
            if (additionJson.has(FIELD_TO_AGGREGATE)) {
                additionItems = additionJson.get(FIELD_TO_AGGREGATE);
            }
            // check if objects not null and has elements, then convert to ArrayNode
            ArrayNode originItemsObj = null;
            if (originItems != null && originItems.isArray() && originItems.elements().hasNext())  {
                originItemsObj = (ArrayNode) originItems;
            }
            ArrayNode additionItemsObj = null;
            if (additionItems != null && additionItems.isArray() && additionItems.elements().hasNext())  {
                additionItemsObj = (ArrayNode) additionItems;
            }
            // if addition features is empty - return only origin data
            if (additionItemsObj == null) {
                return origin;
            }
            if (originItemsObj != null) {
                // only case when we can merge data - both arrays have elements
                ObjectNode root = JsonNodeFactory.instance.objectNode();
                root.put(FIELD_TYPE, COLLECTION);
                root.set(FIELD_TO_AGGREGATE, originItemsObj.addAll(additionItemsObj));
                return root.toString();
            } else {
                // if both feature arrays are empty - return origin data anyway
                return origin;
            }
        } else {
            throw new Exception("Message type is invalid.");
        }
    }

}

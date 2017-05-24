package jp.co.nri.openapi.sample.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

public interface JsonHelper {

	public static class Helper {

		static JsonObjectBuilder convertMapToJson(Map<String, Object> map) {
			JsonObjectBuilder jb = Json.createObjectBuilder();
			map.forEach((k, v) -> {
				if (v instanceof List) {
					jb.add(k, convertListToJson((List<Object>) v));
				} else if (v instanceof Map) {
					jb.add(k, convertMapToJson((Map<String, Object>) v));
				} else {
					jb.add(k, new ConvertableJsonValue(v));
				}
			});
			return jb;
		}

		static JsonArrayBuilder convertListToJson(List<Object> list) {
			JsonArrayBuilder jb = Json.createArrayBuilder();
			list.forEach((v) -> {
				if (v instanceof List) {
					jb.add(convertListToJson((List<Object>) v));
				} else if (v instanceof Map) {
					jb.add(convertMapToJson((Map<String, Object>) v));
				} else {
					jb.add(new ConvertableJsonValue(v));
				}
			});

			return jb;
		}
		
		static Map<String, Object> convertJsonToMap(JsonObject json) {
			Map<String, Object> rst = new HashMap<>();
			json.forEach((k, v) -> {
				rst.put(k, convertJsonToObj(v));
			});
			return rst;
		}
		
		static List<Object> convertJsonToList(JsonArray json) {
			List<Object> rst = new ArrayList<>();
			json.forEach((v) -> {
				rst.add(convertJsonToObj(v));
			});
			return rst;
		}
		
		static Object convertJsonToObj(JsonValue json) {
			if (json instanceof JsonObject) {
				return convertJsonToMap((JsonObject)json);
			} else if (json instanceof JsonArray) {
				return convertJsonToList((JsonArray)json);
			} else if (json instanceof JsonNumber) {
				return ((JsonNumber) json).bigDecimalValue();
			} else if (json instanceof JsonString) {
				return ((JsonString)json).getString();
			} else if (JsonValue.TRUE == json) {
				return true;
			} else if (JsonValue.FALSE == json) {
				return false;
			}
			return null;
		}
	}

	default byte[] map2Json(Map<String, Object> input) {
		JsonObjectBuilder jb = Helper.convertMapToJson(input);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Json.createWriter(stream).write(jb.build());
		return stream.toByteArray();
	}

	default byte[] list2Json(List<Object> input) {
		JsonArrayBuilder jb = Helper.convertListToJson(input);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Json.createWriter(stream).write(jb.build());
		return stream.toByteArray();
	}
	
	default Map<String, Object> json2Map(byte[] input) {
		ByteArrayInputStream stream = new ByteArrayInputStream(input);
		return json2Map(stream);
	}
	
	default List<Object> json2List(byte[] input) {
		ByteArrayInputStream stream = new ByteArrayInputStream(input);
		return json2List(stream);
	}
	
	default Map<String, Object> json2Map(InputStream input) {
		JsonObject json = Json.createParser(input).getObject();
		return Helper.convertJsonToMap(json);
	}

	default List<Object> json2List(InputStream input) {
		JsonArray json = Json.createParser(input).getArray();
		return Helper.convertJsonToList(json);
	}

	public static class ConvertableJsonValue implements JsonValue {
		private Object value;

		public ConvertableJsonValue(Object value) {
			this.value = value;
		}

		@Override
		public ValueType getValueType() {
			if (value instanceof Long || value instanceof Integer || value instanceof Float
					|| value instanceof Double) {
				return ValueType.NUMBER;
			}
			if (value instanceof Boolean) {
				return ((Boolean) value).booleanValue() ? ValueType.TRUE : ValueType.FALSE;
			}

			return ValueType.STRING;
		}

		@Override
		public String toString() {
			return value.toString();
		}
	}

}

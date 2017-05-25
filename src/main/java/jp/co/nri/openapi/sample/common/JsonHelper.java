package jp.co.nri.openapi.sample.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriter;

public interface JsonHelper {
	public static interface ProcessObject {
		void process(Object obj);
	}

	public static class Helper {

		static void addImplicitVars(ProcessObject numberBuilder, ProcessObject stringBuilder, Object obj) {
			if (obj instanceof Integer) {
				numberBuilder.process(new BigDecimal((Integer) obj));
			} else if (obj instanceof Long) {
				numberBuilder.process(new BigDecimal((Long) obj));
			} else if (obj instanceof Float) {
				numberBuilder.process(new BigDecimal((Float) obj));
			} else if (obj instanceof Double) {
				numberBuilder.process(new BigDecimal((Double) obj));
			} else if (obj instanceof BigDecimal) {
				numberBuilder.process((BigDecimal) obj);
			} else {
				stringBuilder.process(obj.toString());
			}
		}

		static JsonObjectBuilder convertMapToJson(Map<String, Object> map) {
			JsonObjectBuilder jb = Json.createObjectBuilder();

			map.forEach((k, v) -> {
				if (v instanceof List) {
					jb.add(k, convertListToJson((List<Object>) v));
				} else if (v instanceof Map) {
					jb.add(k, convertMapToJson((Map<String, Object>) v));
				} else {
					addImplicitVars((o) -> {
						jb.add(k, (BigDecimal) o);
					}, (o) -> {
						jb.add(k, (String) o);
					}, v);
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
					addImplicitVars((o) -> {
						jb.add((BigDecimal)o);
					}, (o) -> {
						jb.add((String)o);
					}, v);
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
				return convertJsonToMap((JsonObject) json);
			} else if (json instanceof JsonArray) {
				return convertJsonToList((JsonArray) json);
			} else if (json instanceof JsonNumber) {
				return ((JsonNumber) json).bigDecimalValue();
			} else if (json instanceof JsonString) {
				return ((JsonString) json).getString();
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
		JsonWriter writer = Json.createWriter(stream);
		writer.write(jb.build());
		writer.close();
		return stream.toByteArray();
	}

	default byte[] list2Json(List<Object> input) {
		JsonArrayBuilder jb = Helper.convertListToJson(input);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		JsonWriter writer = Json.createWriter(stream);
		writer.write(jb.build());
		writer.close();
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
		JsonReader reader = Json.createReader(input);
		JsonObject json = reader.readObject();
		reader.close();
		return Helper.convertJsonToMap(json);
	}

	default List<Object> json2List(InputStream input) {
		JsonReader reader = Json.createReader(input);
		JsonArray json = reader.readArray();
		reader.close();
		return Helper.convertJsonToList(json);
	}

}

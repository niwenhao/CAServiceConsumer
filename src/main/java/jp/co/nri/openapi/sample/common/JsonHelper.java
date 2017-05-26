package jp.co.nri.openapi.sample.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

/**
 * JSON変換するためのヘルパー
 * 
 * @author nwh
 */
public interface JsonHelper {

	public static class Helper {

		/**
		 * JSONは数字一つ種類のみですので、タイプの統一が必要。
		 * @param numberBuilder		数値データの受け取り定義。
		 * @param stringBuilder		文字列データの受け取り定義。
		 * @param obj				処理データ
		 */
		static void addImplicitVars(Consumer<BigDecimal> numberBuilder, Consumer<String> stringBuilder, Object obj) {
			if (obj instanceof Integer) {
				numberBuilder.accept(new BigDecimal((Integer) obj));
			} else if (obj instanceof Long) {
				numberBuilder.accept(new BigDecimal((Long) obj));
			} else if (obj instanceof Float) {
				numberBuilder.accept(new BigDecimal((Float) obj));
			} else if (obj instanceof Double) {
				numberBuilder.accept(new BigDecimal((Double) obj));
			} else if (obj instanceof BigDecimal) {
				numberBuilder.accept((BigDecimal) obj);
			} else {
				stringBuilder.accept(obj.toString());
			}
		}

		/**
		 * JavaのMapをJsonに変換する。
		 * 
		 * @param map	変換対象
		 * @return		mapを取り込んだビルド
		 */
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

		/**
		 * Javaの配列をJsonに変換する。
		 * @param list	変換対象
		 * @return		listを取り込んだビルド
		 */
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

		/**
		 * JsonオブジェクトをJava MAPに変換する
		 * @param json	Jsonオブジェクト
		 * @return		変換結果
		 */
		static Map<String, Object> convertJsonToMap(JsonObject json) {
			Map<String, Object> rst = new HashMap<>();
			json.forEach((k, v) -> {
				rst.put(k, convertJsonToObj(v));
			});
			return rst;
		}

		/**
		 * Jsonの配列をJava Listに変換する。
		 * @param json	変換対象
		 * @return		変換結果
		 */
		static List<Object> convertJsonToList(JsonArray json) {
			List<Object> rst = new ArrayList<>();
			json.forEach((v) -> {
				rst.add(convertJsonToObj(v));
			});
			return rst;
		}

		/**
		 * JSONデータをJavaオブジェクトに変換。
		 * 
		 * @param json		Jsonデータ
		 * @return			変換結果
		 */
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

	/**
	 * Java mapをJsonテキストに変換。
	 * 
	 * @param input	Java map
	 * @return		変換結果
	 */
	default byte[] map2Json(Map<String, Object> input) {
		JsonObjectBuilder jb = Helper.convertMapToJson(input);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		JsonWriter writer = Json.createWriter(stream);
		writer.write(jb.build());
		writer.close();
		return stream.toByteArray();
	}

	/**
	 * Java listをJsonテキストに変換。
	 * 
	 * @param input	Java list
	 * @return		変換結果
	 */
	default byte[] list2Json(List<Object> input) {
		JsonArrayBuilder jb = Helper.convertListToJson(input);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		JsonWriter writer = Json.createWriter(stream);
		writer.write(jb.build());
		writer.close();
		return stream.toByteArray();
	}

	/**
	 * JsonテキストをJava mapに変換。
	 * 
	 * @param input		Jsonテキストバッファ
	 * @return			変換結果
	 */
	default Map<String, Object> json2Map(byte[] input) {
		ByteArrayInputStream stream = new ByteArrayInputStream(input);
		return json2Map(stream);
	}

	/**
	 * JsonテキストをJava listに変換。
	 * 
	 * @param input		Jsonテキストバッファ
	 * @return			変換結果
	 */
	default List<Object> json2List(byte[] input) {
		ByteArrayInputStream stream = new ByteArrayInputStream(input);
		return json2List(stream);
	}

	/**
	 * JsonテキストをJava mapに変換。
	 * 
	 * @param input		Jsonテキストストリーム
	 * @return			変換結果
	 */
	default Map<String, Object> json2Map(InputStream input) {
		JsonReader reader = Json.createReader(input);
		JsonObject json = reader.readObject();
		reader.close();
		return Helper.convertJsonToMap(json);
	}

	/**
	 * JsonテキストをJava listに変換。
	 * 
	 * @param input		Jsonテキストストリーム
	 * @return			変換結果
	 */
	default List<Object> json2List(InputStream input) {
		JsonReader reader = Json.createReader(input);
		JsonArray json = reader.readArray();
		reader.close();
		return Helper.convertJsonToList(json);
	}

}

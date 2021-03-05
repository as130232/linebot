package com.eachnow.linebot.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.http.util.TextUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Json工具
 */
public class JsonUtils {

    private static final ObjectMapper UTIL_OBJECT_MAPPER = getUtilObjectMapper();
    public static final ObjectMapper COMMON_OBJECT_MAPPER = getObjectMapper();

    private static ObjectMapper getUtilObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        //Deserialize
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //Serialize
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return objectMapper;
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }


    /**
     * Java Object轉Json字串
     *
     * @param object 物件
     * @return Json 字串
     * @throws JsonProcessingException
     */
    public static String toJsonString(Object object) throws JsonProcessingException {
        if (object == null)
            return null;

        return UTIL_OBJECT_MAPPER.writeValueAsString(object);
    }

    /**
     * Json字串轉Java Object
     *
     * @param jsonString json 字串
     * @param objectType 要轉成的Java Object
     * @return Java Object
     * @throws IOException
     */
    public static <T> T toObject(String jsonString, Class<T> objectType) throws IOException {
        if (TextUtils.isBlank(jsonString))
            return null;

        return UTIL_OBJECT_MAPPER.readValue(jsonString, objectType);
    }

    /**
     * 將 key/value 轉成 JSON 物件
     *
     * @param kvs 偶數量的 array, 0, 2, 4 ... 為 key, 1, 3, 5... 為 value
     * @return json 字串
     * @throws IllegalArgumentException
     */
    public static String toJsonStringByKeyValue(String... kvs) {
        if (kvs == null)
            return "{}";
        if (kvs.length % 2 != 0)
            throw new IllegalArgumentException("The array length is " + kvs.length + ", it must be even and maps to key/value pairs.");

        String s = "{", prefix = "";

        for (int i = 0; i < kvs.length; i += 2) {
            if (kvs[i] == null)
                throw new IllegalArgumentException("the key cannot be null");

            s += prefix;
            s += ("\"" + kvs[i] + "\":");
            s += (kvs[i + 1] != null ? ("\"" + kvs[i + 1] + "\"") : (kvs[i + 1]));
            prefix = ",";
        }
        s += "}";

        return s;
    }

    /**
     * 將 key/value 轉成 JSON 物件, 此版本使用 + 而不使用 StringBuilder
     *
     * @param values 偶數量的 array, 0, 2, 4 ... 為 key, 1, 3, 5... 為 value
     * @return json 字串
     */
    public static String toJsonStringEx(String... values) {
        if (values == null)
            return "{}";
        if (values.length % 2 != 0)
            throw new IllegalArgumentException("The array length is " + values.length + ", it must be even and maps to key/value pairs.");

        String s = "{", prefix = "";

        for (int i = 0; i < values.length; i += 2) {
            if (values[i] == null)
                throw new IllegalArgumentException("the key cannot be null");

            s += prefix;
            s += ("\"" + values[i] + "\":");
            s += (values[i + 1] != null ? ("\"" + values[i + 1] + "\"") : (values[i + 1]));
            prefix = ",";
        }
        s += "}";

        return s;
    }

    public static String toJsonArrayStringByKeyValue(String[]... arrays) {
        return Stream.of(arrays).map(JsonUtils::toJsonStringByKeyValue).collect(Collectors.toList()).toString();
    }

    /**
     * 將 {@link Collection} 類型轉成 JSON Array
     *
     * @param list
     * @return
     * @throws JsonProcessingException
     */
    public static String toJsonArrayString(Collection list) throws JsonProcessingException {
        return UTIL_OBJECT_MAPPER.writer().writeValueAsString(list);
    }

    /**
     * 是否為 json 陣列
     *
     * @param source
     * @return
     */
    public static boolean isJsonArray(Object source) {
        if ((source instanceof String) == false)
            return false;

        try {
            String str = (String) source;
            return UTIL_OBJECT_MAPPER.readTree(str).isArray();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 物件轉字串
     *
     * @param object 物件
     * @return json 字串
     */
    public static String writeObjectAsString(Object object) {
        if (object == null)
            return null;

        try {
            return COMMON_OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Decode JSON string and parse it to indicate object.
     *
     * @param string     JSON string
     * @param objectType indicate object type
     * @param <T>        object type
     * @return indicate type object
     */
    public static <T> T readStringAsObject(String string, Class<T> objectType) {
        if (TextUtils.isBlank(string)) {
            return null;
        }
        COMMON_OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try {
            return COMMON_OBJECT_MAPPER.readValue(string, objectType);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public static ObjectMapper getCommonObjectMapper() {
        return COMMON_OBJECT_MAPPER;
    }

    /**
     * xml string convert to json string
     *
     * @author charles
     * @date 2019年7月23日 下午5:18:51
     */
//    public static String xml2json(String xml) {
//        try {
//            StringReader input = new StringReader(xml);
//            StringWriter output = new StringWriter();
//            JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(true).autoPrimitive(true).prettyPrint(true).build();
//            try {
//                XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);
//                XMLEventWriter writer = new JsonXMLOutputFactory(config).createXMLEventWriter(output);
//                writer.add(reader);
//                reader.close();
//                writer.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    output.close();
//                    input.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            return output.toString();
//        }catch(Exception e) {
//            throw new RuntimeException("xml to json failed! xml:" + xml);
//        }
//    }
}

package tterrag.core.common.config;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import lombok.Value;
import tterrag.core.common.util.TTFileUtils;

/**
 * A utility to easily read in JSON config files.
 * 
 * The format of the JSON file is as follows:
 * <p>
 * {@code "data":[<br>
 * <br>
 * ]}
 * <p>
 * Where the objects go inbetween the brackets ( [] )
 * 
 * @param <T>
 *            The type of the object to be read in. If this type is generic in itself (ex. {@code HashMap}) you should
 *            use one of the TypeToken
 *            constructors.
 */
public class JsonConfigReader<T> implements Iterable<T> {

    /**
     * A class representing some info about your mod.
     * <p>
     * <b>mainClass</b> is a class in your mod so that files can be copied out of your jar.<br>
     * <b>assetPath</b> is the path to your default json file. This includes the /modid part and whatever subfolders
     * follow.
     */
    @Value
    public static class ModToken {

        private Class<?> mainClass;
        private String assetPath;
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting()
        .create();
    private static final JsonParser parser = new JsonParser();
    private static final String DEFAULT_KEY = "data";

    private File file;
    private JsonObject root;

    private Class<T> type = null;
    private TypeToken<T> typeToken = null;

    /**
     * An easy way to read in config files in JSON format
     * 
     * @param mod
     *                     A {@link ModToken} object to hold data about your mod. This is used to automatically copy the
     *                     file from your jar if it does not
     *                     exist in the config folder
     * @param fullFileName
     *                     The full file path and name of your JSON config. Can be relative to the working directory or
     *                     absolute.
     * @param objClass
     *                     The type of the object being deserialized. Must be of the same type as the generic type of
     *                     this class.
     */
    public JsonConfigReader(ModToken mod, String fullFileName, Class<T> objClass) {
        this(mod, new File(fullFileName), objClass);
    }

    /**
     * An easy way to read in config files in JSON format
     * 
     * @param mod
     *                 A {@link ModToken} object to hold data about your mod. This is used to automatically copy the
     *                 file from your jar if it does not
     *                 exist in the config folder
     * @param file
     *                 The {@link File} representing your JSON config.
     * @param objClass
     *                 The type of the object being deserialized. Must be of the same type as the generic type of this
     *                 class.
     */
    public JsonConfigReader(ModToken mod, File file, Class<T> objClass) {
        this.type = objClass;
        initialize(mod, file);
    }

    /**
     * An easy way to read in config files in JSON format. Use this constructor if the type of this class is generic in
     * itself. (ex.
     * {@code HashMap<K, V>})
     * 
     * @param mod
     *                     A {@link ModToken} object to hold data about your mod. This is used to automatically copy the
     *                     file from your jar if it does not
     *                     exist in the config folder
     * @param fullFileName
     *                     The full file path and name of your JSON config. Can be relative to the working directory or
     *                     absolute.
     * @param objType
     *                     A {@link TypeToken} representing the type of the object to be deserialized. Must be of the
     *                     same type as the generic type of this
     *                     class.
     */
    public JsonConfigReader(ModToken mod, String fullFileName, TypeToken<T> objType) {
        this(mod, new File(fullFileName), objType);
    }

    /**
     * An easy way to read in config files in JSON format. Use this constructor if the type of this class is generic in
     * itself. (ex.
     * {@code HashMap<K, V>})
     * 
     * @param mod
     *                A {@link ModToken} object to hold data about your mod. This is used to automatically copy the file
     *                from your jar if it does not
     *                exist in the config folder
     * @param file
     *                The {@link File} representing your JSON config.
     * @param objType
     *                A {@link TypeToken} representing the type of the object to be deserialized. Must be of the same
     *                type as the generic type of this
     *                class.
     */
    public JsonConfigReader(ModToken mod, File file, TypeToken<T> objType) {
        this.typeToken = objType;
        initialize(mod, file);
    }

    private void initialize(ModToken mod, File file) {
        this.file = file;

        if (!file.exists()) {
            file.getParentFile()
                .mkdirs();
            String assetPath = mod.getAssetPath();
            if (!assetPath.endsWith("/")) {
                assetPath = assetPath + "/";
            }

            TTFileUtils.copyFromJar(mod.getMainClass(), assetPath + file.getName(), file);
        }

        refresh();
    }

    /**
     * Reparses the config file.
     * 
     * @return The {@link JsonObject} read.
     * 
     * @throws RuntimeException
     *                          If there is an exception while reading the file.
     */
    public JsonObject parseFile() {
        try {
            return parser.parse(new FileReader(file))
                .getAsJsonObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reparses the config file and stores the result in this class. Use this if your JSON config was edited and you
     * want to reload from disk.
     * 
     * @throws RuntimeException
     *                          If there is an exception while reading the file.
     */
    public void refresh() {
        this.root = parseFile();
    }

    /**
     * Reads from the {@link JsonObject} linked to the {@link String key} and returns a List of all the elements
     * contained in its array.
     * 
     * @return A list of the generic type of this class containing the deserialized elements from the passed key.
     */
    @SuppressWarnings("unchecked")
    public List<T> getElements(String key) {
        JsonArray elements = root.get(key)
            .getAsJsonArray();
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < elements.size(); i++) {
            if (type == null) {
                list.add((T) gson.fromJson(elements.get(i), typeToken.getType()));
            } else {
                list.add(gson.fromJson(elements.get(i), type));
            }
        }
        return list;
    }

    /**
     * Reads the default (defined by {@value #DEFAULT_KEY}) cached JsonObject in this class and returns a List of all
     * the elements contained in its
     * array.
     * <p>
     * {@link #getElements(String)} should be used if your config has multiple keys.
     * 
     * @return A list of the generic type of this class containing the deserialized elements from your JSON config's
     *         default key.
     */
    public List<T> getElements() {
        return getElements(DEFAULT_KEY);
    }

    @Override
    public Iterator<T> iterator() {
        return getElements().iterator();
    }
}

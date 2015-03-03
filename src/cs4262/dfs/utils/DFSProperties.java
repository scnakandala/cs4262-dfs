package cs4262.dfs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class DFSProperties {

    private static DFSProperties instance;

    private static final String PROPERTY_FILE="dfs.properties";
    
    private Properties properties = null;

    private DFSProperties() {
        try {
            InputStream fileInput;
            fileInput = new FileInputStream(PROPERTY_FILE);
            Properties properties = new Properties();
            properties.load(fileInput);
            fileInput.close();
            this.properties = properties;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DFSProperties getInstance() {
        if (DFSProperties.instance == null) {
            DFSProperties.instance = new DFSProperties();
        }

        return DFSProperties.instance;
    }

    public String getProperty(String key, String defaultVal) {
        String val = this.properties.getProperty(key);
        if (val.isEmpty() || val == "") {
            return defaultVal;
        } else {
            return val;
        }
    }

    public boolean containsKey(String loadBalancingPolicy) {
        return this.properties.containsKey(loadBalancingPolicy);
    }
}

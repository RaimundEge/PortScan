import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.WriteResult;


public class DB {
    DBCollection col;
    static DB db = new DB();

    void initConnection() {
        // check for exisiting connection
        if (col == null) {
            /* establish connection to DB */
            try {

                MongoClient mongo = new MongoClient("localhost", 27017);
                DBCollection col = mongo.getDB("csci350").getCollection("logrecords");

            } catch (Exception ex) {
                System.out.println("DB error: " + ex);
            }
    }
    }

    void write(String groupName, String type, String address, int port, String record) {
        String addr = address.substring(1);
        // System.out.println("writing: " + type + ", " + addr + ", " + port + ", " +
        // record);
        try {
            initConnection();
            DBObject logrecord = BasicDBObjectBuilder.start();
            logrecord.add("groupName", groupName);
            logrecord.add("type", type);  
            logrecord.add("addr", addr);
            logrecord.add("port", port);
            logrecord.add("record", record);                        
            
            WriteResult result = col.insert(logrecord);
        } catch (Exception ex) {
            System.out.println("DB error: " + ex);
        }
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}

package portscan;

import org.bson.*;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import java.util.Date;


public class DB {
    MongoCollection<Document> col;
    static DB db = new DB();

    void initConnection() {
        // check for exisiting connection
        if (col == null) {
            /* establish connection to DB */
            try {
                ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
                MongoClient mongo = MongoClients.create(connectionString);
                col = mongo.getDatabase("csci350").getCollection("logrecords");
            } catch (Exception ex) {
                System.out.println("DB error: " + ex);
            }
    }
    }

    void write(String groupName, String type, String address, int port, String record) {
        String addr = address.substring(1);
    
        try {
            initConnection();

             Document logrecord = new Document("groupName", groupName)
                 .append("timestamp", new Date())
                 .append("type", type)
                 .append("IP", addr)
                 .append("port", port)
                 .append("record", record);

            col.insertOne(logrecord);
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

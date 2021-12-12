import 'dart:async';
import 'package:intl/intl.dart';
import 'package:mysql1/mysql1.dart';

class DB {
  Future<MySqlConnection> initConn() {
    return MySqlConnection.connect(ConnectionSettings(
        host: 'localhost',
        port: 3306,
        user: 'instructor',
        password: 'instructor',
        db: 'csci350'));
  }

  Future<List<dynamic>> getLogs(int recent) async {
    var conn = await initConn();
    var list = [];
    var where = 'where timestamp > now() - INTERVAL ${recent} MONTH ';
    var results = await conn.query(
        'select id, timestamp, groupName, type, IP, port from logrecords ' + where + 'ORDER BY id DESC');
    for (var row in results) {
      var logrecord = {
        'id': row[0],
        'timestamp': DateFormat.MMMd().add_Hm().format(row[1].toLocal()),
        'group': row[2],
        'type': row[3],
        'IP': row[4],
        'port': row[5]
      };
      list.add(logrecord);
    }
    print('${results.length} log records found');
    await conn.close();
    return list;
  }

  Future<dynamic> getLog(int id) async {
    var conn = await initConn();
    var record;
    // Query the database using a parameterized query
    var results = await conn.query('select * from logrecords where id = "$id"');
    print('${results.length} log record found');
    for (var row in results) {
      record = {
        'id': row[0],
        'timestamp': row[1].toLocal().toString(),
        'group': row[2],
        'type': row[3],
        'IP': row[4],
        'port': row[5],
        'record': row[6]
      };
    }
    await conn.close();
    return record;
  }
}

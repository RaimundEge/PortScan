import 'package:port_scan_log/db.dart';
import 'dart:convert';
import 'package:shelf/shelf.dart';
import 'package:shelf/shelf_io.dart' as io;

var db = DB();

void main() async {
  var handler =
      const Pipeline().addMiddleware(logRequests()).addHandler(dbRequest);

  var server = await io.serve(handler, 'localhost', 8338);

  // Enable content compression
  server.autoCompress = true;

  print('Serving at http://${server.address.host}:${server.port}');
}

Future<Response> dbRequest(Request request) async {
  switch (request.method) {
    case 'GET':
      switch (request.url.path) {
        case 'records':
          var count = 12;
          if (request.url.hasQuery) {
            var m = int.tryParse(request.url.queryParameters['month']);
            if (m != null) {
              count = m;
            }
          }
          var list = await db.getLogs(count);
          return Response.ok(json.encode(list));
          break;
        case 'record':
          if (request.url.hasQuery) {
            var id = int.tryParse(request.url.queryParameters['id']);
            if (id != null) {
              var record = await db.getLog(id);
              if (record != null) {
                return Response.ok(json.encode(record));
              }
            }
          }
          return Response.notFound('id parameter missing or invalid');
          break;
        default:
          return Response.ok(
              'you have reached the log records server for the CSCI 350 port scan assignment');
      }
      break;
    default:
      return Response.notFound('not found');
  }
}

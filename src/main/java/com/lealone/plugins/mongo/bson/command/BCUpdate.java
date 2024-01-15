/*
 * Copyright Lealone Database Group.
 * Licensed under the Server Side Public License, v 1.
 * Initial Developer: zhh
 */
package com.lealone.plugins.mongo.bson.command;

import java.util.ArrayList;

import org.bson.BsonDocument;
import org.bson.io.ByteBufferBsonInput;

import com.lealone.db.session.ServerSession;
import com.lealone.db.table.Table;
import com.lealone.plugins.mongo.bson.operator.BOUpdateOperator;
import com.lealone.plugins.mongo.server.MongoServerConnection;
import com.lealone.plugins.mongo.server.MongoTask;
import com.lealone.sql.dml.Update;
import com.lealone.sql.optimizer.TableFilter;

public class BCUpdate extends BsonCommand {

    public static BsonDocument execute(ByteBufferBsonInput input, BsonDocument doc,
            MongoServerConnection conn, MongoTask task) {
        Table table = findTable(doc, "update", conn);
        if (table != null) {
            update(input, doc, conn, table, task);
            return null;
        } else {
            return createResponseDocument(0);
        }
    }

    private static void update(ByteBufferBsonInput input, BsonDocument doc, MongoServerConnection conn,
            Table table, MongoTask task) {
        ServerSession session = task.session;
        Update update = new Update(session);
        TableFilter tableFilter = new TableFilter(session, table, null, true, null);
        update.setTableFilter(tableFilter);
        ArrayList<BsonDocument> updates = readPayload(input, doc, conn, "updates");
        for (BsonDocument updateDoc : updates) {
            BsonDocument q = updateDoc.getDocument("q", null);
            BsonDocument u = updateDoc.getDocument("u", null);
            if (q != null) {
                update.setCondition(toWhereCondition(q, tableFilter, session));
            }
            if (u != null) {
                BOUpdateOperator.setAssignment(u, tableFilter, session, table, update);
            }
        }
        update.prepare();
        createAndSubmitYieldableUpdate(task, update);
    }
}

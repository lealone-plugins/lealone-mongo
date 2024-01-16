/*
 * Copyright Lealone Database Group.
 * Licensed under the Server Side Public License, v 1.
 * Initial Developer: zhh
 */
package com.lealone.plugins.mongo.server;

import com.lealone.db.Database;
import com.lealone.db.LealoneDatabase;
import com.lealone.db.scheduler.Scheduler;
import com.lealone.db.session.ServerSession;
import com.lealone.net.WritableChannel;
import com.lealone.plugins.mongo.MongoPlugin;
import com.lealone.server.AsyncServer;

public class MongoServer extends AsyncServer<MongoServerConnection> {

    public static final String DATABASE_NAME = "mongo";
    public static final int DEFAULT_PORT = 27017;

    @Override
    public String getType() {
        return MongoPlugin.NAME;
    }

    @Override
    public synchronized void start() {
        super.start();

        // 创建默认的 mongodb 数据库
        String sql = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
        Database db = LealoneDatabase.getInstance();
        try (ServerSession session = db.createSession(db.getSystemUser())) {
            session.executeUpdateLocal(sql);
        }
    }

    @Override
    protected int getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    protected MongoServerConnection createConnection(WritableChannel writableChannel,
            Scheduler scheduler) {
        return new MongoServerConnection(this, writableChannel, scheduler, getConnectionSize() + 1);
    }
}

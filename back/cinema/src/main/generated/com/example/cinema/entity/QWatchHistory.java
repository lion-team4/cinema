package com.example.cinema.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWatchHistory is a Querydsl query type for WatchHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWatchHistory extends EntityPathBase<WatchHistory> {

    private static final long serialVersionUID = -818645724L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWatchHistory watchHistory = new QWatchHistory("watchHistory");

    public final com.example.cinema.entity.common.QBaseEntity _super = new com.example.cinema.entity.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> enterAt = createDateTime("enterAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> leftAt = createDateTime("leftAt", java.time.LocalDateTime.class);

    public final QScheduleItem scheduleItem;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public final BooleanPath viewCounted = createBoolean("viewCounted");

    public final NumberPath<Long> watchId = createNumber("watchId", Long.class);

    public QWatchHistory(String variable) {
        this(WatchHistory.class, forVariable(variable), INITS);
    }

    public QWatchHistory(Path<? extends WatchHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWatchHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWatchHistory(PathMetadata metadata, PathInits inits) {
        this(WatchHistory.class, metadata, inits);
    }

    public QWatchHistory(Class<? extends WatchHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.scheduleItem = inits.isInitialized("scheduleItem") ? new QScheduleItem(forProperty("scheduleItem"), inits.get("scheduleItem")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}


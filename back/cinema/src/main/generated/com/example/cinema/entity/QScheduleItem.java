package com.example.cinema.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QScheduleItem is a Querydsl query type for ScheduleItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScheduleItem extends EntityPathBase<ScheduleItem> {

    private static final long serialVersionUID = 1082675881L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QScheduleItem scheduleItem = new QScheduleItem("scheduleItem");

    public final com.example.cinema.entity.common.QBaseEntity _super = new com.example.cinema.entity.common.QBaseEntity(this);

    public final QContent content;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> endAt = createDateTime("endAt", java.time.LocalDateTime.class);

    public final QScheduleDay scheduleDay;

    public final NumberPath<Long> scheduleItemId = createNumber("scheduleItemId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> startAt = createDateTime("startAt", java.time.LocalDateTime.class);

    public final EnumPath<com.example.cinema.type.ScheduleStatus> status = createEnum("status", com.example.cinema.type.ScheduleStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QScheduleItem(String variable) {
        this(ScheduleItem.class, forVariable(variable), INITS);
    }

    public QScheduleItem(Path<? extends ScheduleItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QScheduleItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QScheduleItem(PathMetadata metadata, PathInits inits) {
        this(ScheduleItem.class, metadata, inits);
    }

    public QScheduleItem(Class<? extends ScheduleItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.content = inits.isInitialized("content") ? new QContent(forProperty("content"), inits.get("content")) : null;
        this.scheduleDay = inits.isInitialized("scheduleDay") ? new QScheduleDay(forProperty("scheduleDay"), inits.get("scheduleDay")) : null;
    }

}


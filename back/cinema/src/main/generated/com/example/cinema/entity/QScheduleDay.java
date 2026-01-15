package com.example.cinema.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QScheduleDay is a Querydsl query type for ScheduleDay
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScheduleDay extends EntityPathBase<ScheduleDay> {

    private static final long serialVersionUID = -934911674L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QScheduleDay scheduleDay = new QScheduleDay("scheduleDay");

    public final com.example.cinema.entity.common.QBaseEntity _super = new com.example.cinema.entity.common.QBaseEntity(this);

    public final QContent content;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final BooleanPath isLocked = createBoolean("isLocked");

    public final DateTimePath<java.time.LocalDateTime> lockedAt = createDateTime("lockedAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> scheduleDate = createDate("scheduleDate", java.time.LocalDate.class);

    public final NumberPath<Long> scheduleDayId = createNumber("scheduleDayId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QScheduleDay(String variable) {
        this(ScheduleDay.class, forVariable(variable), INITS);
    }

    public QScheduleDay(Path<? extends ScheduleDay> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QScheduleDay(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QScheduleDay(PathMetadata metadata, PathInits inits) {
        this(ScheduleDay.class, metadata, inits);
    }

    public QScheduleDay(Class<? extends ScheduleDay> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.content = inits.isInitialized("content") ? new QContent(forProperty("content"), inits.get("content")) : null;
    }

}


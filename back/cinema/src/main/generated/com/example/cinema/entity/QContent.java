package com.example.cinema.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;
import com.example.cinema.repository.content.custom.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QContent is a Querydsl query type for Content
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContent extends EntityPathBase<Content> {

    private static final long serialVersionUID = -82722630L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContent content = new QContent("content");

    public final com.example.cinema.entity.common.QBaseEntity _super = new com.example.cinema.entity.common.QBaseEntity(this);

    public final NumberPath<Long> contentId = createNumber("contentId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final StringPath encodingError = createString("encodingError");

    public final EnumPath<com.example.cinema.type.EncodingStatus> encodingStatus = createEnum("encodingStatus", com.example.cinema.type.EncodingStatus.class);

    public final NumberPath<Long> monthView = createNumber("monthView", Long.class);

    public final QUser owner;

    public final QMediaAsset poster;

    public final EnumPath<com.example.cinema.type.ContentStatus> status = createEnum("status", com.example.cinema.type.ContentStatus.class);

    public final ListPath<TagMap, QTagMap> tagMaps = this.<TagMap, QTagMap>createList("tagMaps", TagMap.class, QTagMap.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    public final NumberPath<Long> totalView = createNumber("totalView", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QMediaAsset videoHlsMaster;

    public final QMediaAsset videoSource;

    public QContent(String variable) {
        this(Content.class, forVariable(variable), INITS);
    }

    public QContent(Path<? extends Content> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContent(PathMetadata metadata, PathInits inits) {
        this(Content.class, metadata, inits);
    }

    public QContent(Class<? extends Content> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.owner = inits.isInitialized("owner") ? new QUser(forProperty("owner"), inits.get("owner")) : null;
        this.poster = inits.isInitialized("poster") ? new QMediaAsset(forProperty("poster"), inits.get("poster")) : null;
        this.videoHlsMaster = inits.isInitialized("videoHlsMaster") ? new QMediaAsset(forProperty("videoHlsMaster"), inits.get("videoHlsMaster")) : null;
        this.videoSource = inits.isInitialized("videoSource") ? new QMediaAsset(forProperty("videoSource"), inits.get("videoSource")) : null;
    }

    public com.querydsl.core.types.Predicate search(com.example.cinema.dto.content.ContentSearchRequest request) {
        return ContentExpression.search(this, request);
    }

    public com.querydsl.core.types.OrderSpecifier<?> sort(com.example.cinema.dto.content.ContentSearchRequest request) {
        return ContentExpression.sort(this, request);
    }

}


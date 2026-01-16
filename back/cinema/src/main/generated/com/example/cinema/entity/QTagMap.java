package com.example.cinema.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTagMap is a Querydsl query type for TagMap
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTagMap extends EntityPathBase<TagMap> {

    private static final long serialVersionUID = -2023000319L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTagMap tagMap = new QTagMap("tagMap");

    public final QContent content;

    public final QTag tag;

    public final NumberPath<Long> tagMapId = createNumber("tagMapId", Long.class);

    public QTagMap(String variable) {
        this(TagMap.class, forVariable(variable), INITS);
    }

    public QTagMap(Path<? extends TagMap> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTagMap(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTagMap(PathMetadata metadata, PathInits inits) {
        this(TagMap.class, metadata, inits);
    }

    public QTagMap(Class<? extends TagMap> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.content = inits.isInitialized("content") ? new QContent(forProperty("content"), inits.get("content")) : null;
        this.tag = inits.isInitialized("tag") ? new QTag(forProperty("tag")) : null;
    }

}


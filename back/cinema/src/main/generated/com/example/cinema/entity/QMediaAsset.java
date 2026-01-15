package com.example.cinema.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMediaAsset is a Querydsl query type for MediaAsset
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMediaAsset extends EntityPathBase<MediaAsset> {

    private static final long serialVersionUID = -1725933173L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMediaAsset mediaAsset = new QMediaAsset("mediaAsset");

    public final com.example.cinema.entity.common.QBaseEntity _super = new com.example.cinema.entity.common.QBaseEntity(this);

    public final NumberPath<Long> assetId = createNumber("assetId", Long.class);

    public final EnumPath<com.example.cinema.type.AssetType> assetType = createEnum("assetType", com.example.cinema.type.AssetType.class);

    public final StringPath bucket = createString("bucket");

    public final StringPath contentType = createString("contentType");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> durationMs = createNumber("durationMs", Long.class);

    public final StringPath objectKey = createString("objectKey");

    public final QUser owner;

    public final NumberPath<Long> sizeBytes = createNumber("sizeBytes", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final EnumPath<com.example.cinema.type.Visibility> visibility = createEnum("visibility", com.example.cinema.type.Visibility.class);

    public QMediaAsset(String variable) {
        this(MediaAsset.class, forVariable(variable), INITS);
    }

    public QMediaAsset(Path<? extends MediaAsset> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMediaAsset(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMediaAsset(PathMetadata metadata, PathInits inits) {
        this(MediaAsset.class, metadata, inits);
    }

    public QMediaAsset(Class<? extends MediaAsset> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.owner = inits.isInitialized("owner") ? new QUser(forProperty("owner"), inits.get("owner")) : null;
    }

}


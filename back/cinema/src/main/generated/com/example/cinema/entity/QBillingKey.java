package com.example.cinema.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBillingKey is a Querydsl query type for BillingKey
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBillingKey extends EntityPathBase<BillingKey> {

    private static final long serialVersionUID = 1491152387L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBillingKey billingKey1 = new QBillingKey("billingKey1");

    public final com.example.cinema.entity.common.QBaseEntity _super = new com.example.cinema.entity.common.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> authenticatedAt = createDateTime("authenticatedAt", java.time.LocalDateTime.class);

    public final StringPath billingKey = createString("billingKey");

    public final NumberPath<Long> billingKeyId = createNumber("billingKeyId", Long.class);

    public final StringPath cardCompany = createString("cardCompany");

    public final StringPath cardNumber = createString("cardNumber");

    public final StringPath cardType = createString("cardType");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath customerKey = createString("customerKey");

    public final StringPath ownerType = createString("ownerType");

    public final EnumPath<com.example.cinema.type.BillingProvider> provider = createEnum("provider", com.example.cinema.type.BillingProvider.class);

    public final DateTimePath<java.time.LocalDateTime> revokedAt = createDateTime("revokedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.example.cinema.type.BillingKeyStatus> status = createEnum("status", com.example.cinema.type.BillingKeyStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QBillingKey(String variable) {
        this(BillingKey.class, forVariable(variable), INITS);
    }

    public QBillingKey(Path<? extends BillingKey> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBillingKey(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBillingKey(PathMetadata metadata, PathInits inits) {
        this(BillingKey.class, metadata, inits);
    }

    public QBillingKey(Class<? extends BillingKey> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}


package com.example.cinema.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSubscription is a Querydsl query type for Subscription
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubscription extends EntityPathBase<Subscription> {

    private static final long serialVersionUID = 262032764L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSubscription subscription = new QSubscription("subscription");

    public final com.example.cinema.entity.common.QBaseEntity _super = new com.example.cinema.entity.common.QBaseEntity(this);

    public final QBillingKey billingKey;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> currentPeriodEnd = createDateTime("currentPeriodEnd", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> currentPeriodStart = createDateTime("currentPeriodStart", java.time.LocalDateTime.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath name = createString("name");

    public final NumberPath<Long> price = createNumber("price", Long.class);

    public final EnumPath<com.example.cinema.type.SubscriptionStatus> status = createEnum("status", com.example.cinema.type.SubscriptionStatus.class);

    public final QUser subscriber;

    public final NumberPath<Long> subscriptionId = createNumber("subscriptionId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSubscription(String variable) {
        this(Subscription.class, forVariable(variable), INITS);
    }

    public QSubscription(Path<? extends Subscription> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSubscription(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSubscription(PathMetadata metadata, PathInits inits) {
        this(Subscription.class, metadata, inits);
    }

    public QSubscription(Class<? extends Subscription> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.billingKey = inits.isInitialized("billingKey") ? new QBillingKey(forProperty("billingKey"), inits.get("billingKey")) : null;
        this.subscriber = inits.isInitialized("subscriber") ? new QUser(forProperty("subscriber"), inits.get("subscriber")) : null;
    }

}


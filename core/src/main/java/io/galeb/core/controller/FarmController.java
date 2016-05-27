package io.galeb.core.controller;

import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;

public class FarmController extends EntityController {

    private BackendController backendController;
    private BackendPoolController backendPoolController;
    private RuleController ruleController;
    private VirtualHostController virtualHostController;

    public FarmController(final Farm farm) {
        super(farm);
    }

    public FarmController setBackendController(BackendController backendController) {
        this.backendController = backendController;
        return this;
    }

    public FarmController setBackendPoolController(BackendPoolController backendPoolController) {
        this.backendPoolController = backendPoolController;
        return this;
    }

    public FarmController setRuleController(RuleController ruleController) {
        this.ruleController = ruleController;
        return this;
    }

    public FarmController setVirtualHostController(VirtualHostController virtualHostController) {
        this.virtualHostController = virtualHostController;
        return this;
    }

    @Override
    public EntityController add(Entity entity) throws Exception {
        for (Entity backendPool : ((Farm) entity).getCollection(BackendPool.class)) {
            backendPoolController.add(backendPool.copy());
        }
        for (Entity backend : ((Farm) entity).getCollection(Backend.class)) {
            backendController.add(backend.copy());
        }
        for (Entity virtualhost : ((Farm) entity).getCollection(VirtualHost.class)) {
            virtualHostController.add(virtualhost.copy());
        }
        for (Entity rule : ((Farm) entity).getCollection(Rule.class)) {
            ruleController.add(rule.copy());
        }
        setVersion(entity.getVersion());
        return this;
    }

    @Override
    public EntityController del(Entity entity) throws Exception {
        delAll();
        return this;
    }

    @Override
    public EntityController delAll() throws Exception {
        delAll(Backend.class);
        delAll(BackendPool.class);
        delAll(Rule.class);
        delAll(VirtualHost.class);
        setVersion(0);
        return this;
    }

    @Override
    public EntityController change(Entity entity) throws Exception {
        for (final Entity backendPool: ((Farm) entity).getCollection(BackendPool.class)) {
            backendPoolController.change(backendPool);
        }
        for (Entity backend : ((Farm) entity).getCollection(Backend.class)) {
            backendController.change(backend.copy());
        }
        for (final Entity virtualhost: ((Farm) entity).getCollection(VirtualHost.class)) {
            virtualHostController.change(virtualhost);
        }
        for (Entity rule : ((Farm) entity).getCollection(Rule.class)) {
            ruleController.change(rule.copy());
        }
        setVersion(entity.getVersion());
        return this;
    }

    @Override
    public String get(String id) {
        return get(Farm.class, null);
    }

}

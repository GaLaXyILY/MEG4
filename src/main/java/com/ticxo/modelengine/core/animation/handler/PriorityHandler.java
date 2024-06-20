package com.ticxo.modelengine.core.animation.handler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.AnimationPropertyRegistry;
import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.ModelState;
import com.ticxo.modelengine.api.animation.Timeline;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.handler.IPriorityHandler;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeType;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeTypeRegistry;
import com.ticxo.modelengine.api.animation.keyframe.type.AbstractKeyframe;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.animation.property.SimpleProperty;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.events.AnimationEndEvent;
import com.ticxo.modelengine.api.events.AnimationPlayEvent;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.api.utils.state.StateMachine;
import com.ticxo.modelengine.api.utils.state.StateNode;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.Nullable;

public class PriorityHandler implements IPriorityHandler {
   private final ActiveModel activeModel;
   private final ModelBlueprint blueprint;
   private final Map<String, IAnimationProperty> properties = Maps.newConcurrentMap();
   private final Map<String, IAnimationProperty> updatedProperties = Maps.newConcurrentMap();
   private final Map<ModelState, AnimationHandler.DefaultProperty> defaultProperties = Maps.newConcurrentMap();
   private final StateMachine<BaseEntity<?>> stateMachine = new StateMachine();
   private boolean firstSpawn = true;

   public PriorityHandler(ActiveModel activeModel) {
      this.activeModel = activeModel;
      this.blueprint = activeModel.getBlueprint();
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.IDLE, 0.25D, 0.25D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.WALK, 0.25D, 0.25D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.STRAFE, 0.25D, 0.25D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.JUMP_START, 0.0D, 0.25D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.JUMP, 0.0D, 0.0D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.JUMP_END, 0.0D, 0.25D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.HOVER, 0.25D, 0.25D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.FLY, 0.25D, 0.25D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.SPAWN, 0.0D, 0.25D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.DEATH, 0.0D, 0.0D, 1.0D));
      this.configureAnimation();
   }

   private void configureAnimation() {
      StateNode<BaseEntity<?>> spawn = this.stateMachine.createNode();
      StateNode<BaseEntity<?>> idle = this.stateMachine.createNode();
      StateNode<BaseEntity<?>> walk = this.stateMachine.createNode();
      StateNode<BaseEntity<?>> strafe = this.stateMachine.createNode();
      StateNode<BaseEntity<?>> jumpStart = this.stateMachine.createNode();
      StateNode<BaseEntity<?>> jumpLoop = this.stateMachine.createNode();
      StateNode<BaseEntity<?>> jumpEnd = this.stateMachine.createNode();
      StateNode<BaseEntity<?>> hover = this.stateMachine.createNode();
      StateNode<BaseEntity<?>> fly = this.stateMachine.createNode();
      StateNode<BaseEntity<?>> death = this.stateMachine.createNode();
      spawn.setExitAction((baseEntity) -> {
         this.playState(ModelState.SPAWN);
      });
      spawn.addConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      spawn.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && baseEntity.isWalking() && this.hasAnimation(ModelState.FLY);
      }, fly);
      spawn.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && this.hasAnimation(ModelState.HOVER);
      }, hover);
      spawn.addConnectedNode((baseEntity) -> {
         return baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP_START);
      }, jumpStart);
      spawn.addConnectedNode((baseEntity) -> {
         return baseEntity.isStrafing() && this.hasAnimation(ModelState.STRAFE);
      }, strafe);
      spawn.addConnectedNode(BaseEntity::isWalking, walk);
      spawn.addConnectedNode((baseEntity) -> {
         return !baseEntity.isWalking();
      }, idle);
      idle.setEntryAction((baseEntity) -> {
         this.playState(ModelState.IDLE);
      });
      idle.addForceConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      idle.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && baseEntity.isWalking() && this.hasAnimation(ModelState.FLY);
      }, fly);
      idle.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && this.hasAnimation(ModelState.HOVER);
      }, hover);
      idle.addConnectedNode((baseEntity) -> {
         return baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP_START);
      }, jumpStart);
      idle.addConnectedNode((baseEntity) -> {
         return baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP);
      }, jumpLoop);
      idle.addConnectedNode((baseEntity) -> {
         return baseEntity.isStrafing() && this.hasAnimation(ModelState.STRAFE);
      }, strafe);
      idle.addConnectedNode(BaseEntity::isWalking, walk);
      idle.setExitAction((baseEntity) -> {
         this.stopState(ModelState.IDLE);
      });
      walk.setEntryAction((baseEntity) -> {
         this.playState(ModelState.WALK);
      });
      walk.addForceConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      walk.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && baseEntity.isWalking() && this.hasAnimation(ModelState.FLY);
      }, fly);
      walk.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && this.hasAnimation(ModelState.HOVER);
      }, hover);
      walk.addConnectedNode((baseEntity) -> {
         return baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP_START);
      }, jumpStart);
      walk.addConnectedNode((baseEntity) -> {
         return baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP);
      }, jumpLoop);
      walk.addConnectedNode((baseEntity) -> {
         return baseEntity.isStrafing() && this.hasAnimation(ModelState.STRAFE);
      }, strafe);
      walk.addConnectedNode((baseEntity) -> {
         return !baseEntity.isWalking();
      }, idle);
      walk.setExitAction((baseEntity) -> {
         this.stopState(ModelState.WALK);
      });
      strafe.setEntryAction((baseEntity) -> {
         this.playState(ModelState.STRAFE);
      });
      strafe.addForceConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      strafe.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && baseEntity.isWalking() && this.hasAnimation(ModelState.FLY);
      }, fly);
      strafe.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && this.hasAnimation(ModelState.HOVER);
      }, hover);
      strafe.addConnectedNode((baseEntity) -> {
         return baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP_START);
      }, jumpStart);
      strafe.addConnectedNode((baseEntity) -> {
         return baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP);
      }, jumpLoop);
      strafe.addConnectedNode(BaseEntity::isWalking, walk);
      strafe.addConnectedNode((baseEntity) -> {
         return !baseEntity.isWalking();
      }, idle);
      strafe.setExitAction((baseEntity) -> {
         this.stopState(ModelState.STRAFE);
      });
      jumpStart.setEntryAction((baseEntity) -> {
         this.playState(ModelState.JUMP_START);
      });
      jumpStart.addForceConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      jumpStart.setCommonPredicate((baseEntity) -> {
         return this.hasFinishedPlaying(ModelState.JUMP_START);
      });
      jumpStart.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && baseEntity.isWalking() && this.hasAnimation(ModelState.FLY);
      }, fly);
      jumpStart.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && this.hasAnimation(ModelState.HOVER);
      }, hover);
      jumpStart.addConnectedNode((baseEntity) -> {
         return !baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP_END);
      }, jumpEnd);
      jumpStart.addConnectedNode((baseEntity) -> {
         return !baseEntity.isJumping() && baseEntity.isWalking();
      }, walk);
      jumpStart.addConnectedNode((baseEntity) -> {
         return !baseEntity.isJumping() && !baseEntity.isWalking();
      }, idle);
      jumpStart.addConnectedNode((baseEntity) -> {
         return this.hasFinishedPlaying(ModelState.JUMP_START) && this.hasAnimation(ModelState.JUMP);
      }, jumpLoop);
      jumpLoop.setEntryAction((baseEntity) -> {
         this.playState(ModelState.JUMP);
      });
      jumpLoop.addForceConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      jumpLoop.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && baseEntity.isWalking() && this.hasAnimation(ModelState.FLY);
      }, fly);
      jumpLoop.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && this.hasAnimation(ModelState.HOVER);
      }, hover);
      jumpLoop.addConnectedNode((baseEntity) -> {
         return !baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP_END);
      }, jumpEnd);
      jumpLoop.addConnectedNode((baseEntity) -> {
         return !baseEntity.isJumping() && baseEntity.isWalking();
      }, walk);
      jumpLoop.addConnectedNode((baseEntity) -> {
         return !baseEntity.isJumping() && !baseEntity.isWalking();
      }, idle);
      jumpLoop.setExitAction((baseEntity) -> {
         this.stopState(ModelState.JUMP);
      });
      jumpEnd.setEntryAction((baseEntity) -> {
         this.playState(ModelState.JUMP_END);
      });
      jumpEnd.addForceConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      jumpEnd.setCommonPredicate((baseEntity) -> {
         return this.hasFinishedPlaying(ModelState.JUMP_END);
      });
      jumpEnd.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && baseEntity.isWalking() && this.hasAnimation(ModelState.FLY);
      }, fly);
      jumpEnd.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && this.hasAnimation(ModelState.HOVER);
      }, hover);
      jumpEnd.addConnectedNode((baseEntity) -> {
         return baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP_START);
      }, jumpStart);
      jumpEnd.addConnectedNode((baseEntity) -> {
         return baseEntity.isStrafing() && this.hasAnimation(ModelState.STRAFE);
      }, strafe);
      jumpEnd.addConnectedNode(BaseEntity::isWalking, walk);
      jumpEnd.addConnectedNode((baseEntity) -> {
         return !baseEntity.isWalking();
      }, idle);
      hover.setEntryAction((baseEntity) -> {
         this.playState(ModelState.HOVER);
      });
      hover.addConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      hover.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && baseEntity.isWalking() && this.hasAnimation(ModelState.FLY);
      }, fly);
      hover.addConnectedNode((baseEntity) -> {
         return !baseEntity.isFlying() && baseEntity.isStrafing() && this.hasAnimation(ModelState.STRAFE);
      }, strafe);
      hover.addConnectedNode((baseEntity) -> {
         return !baseEntity.isFlying() && baseEntity.isWalking();
      }, walk);
      hover.addConnectedNode((baseEntity) -> {
         return !baseEntity.isFlying() && !baseEntity.isWalking();
      }, idle);
      hover.setExitAction((baseEntity) -> {
         this.stopState(ModelState.HOVER);
      });
      fly.setEntryAction((baseEntity) -> {
         this.playState(ModelState.FLY);
      });
      fly.addForceConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      fly.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && !baseEntity.isWalking() && this.hasAnimation(ModelState.HOVER);
      }, hover);
      fly.addConnectedNode((baseEntity) -> {
         return !baseEntity.isFlying() && baseEntity.isStrafing() && this.hasAnimation(ModelState.STRAFE);
      }, strafe);
      fly.addConnectedNode((baseEntity) -> {
         return !baseEntity.isFlying() && baseEntity.isWalking();
      }, walk);
      fly.addConnectedNode((baseEntity) -> {
         return !baseEntity.isFlying() && !baseEntity.isWalking();
      }, idle);
      fly.setExitAction((baseEntity) -> {
         this.stopState(ModelState.FLY);
      });
      death.setEntryAction((baseEntity) -> {
         this.forceStopAllAnimations();
         this.playState(ModelState.DEATH);
      });
      this.stateMachine.setEntryNode(spawn);
   }

   private boolean hasAnimation(ModelState state) {
      return this.blueprint.getAnimations().containsKey(state.getString());
   }

   private boolean hasFinishedPlaying(ModelState state) {
      IAnimationProperty animation = this.getAnimation(state.getString());
      return animation == null || animation.getPhase() == IAnimationProperty.Phase.LERPOUT;
   }

   private void stopState(ModelState state) {
      AnimationHandler.DefaultProperty defProperty = this.getDefaultProperty(state);
      this.stopAnimation(defProperty.getAnimation());
   }

   public void prepare() {
      this.stateMachine.execute(this.activeModel.getModeledEntity().getBase());
      this.firstSpawn = false;
      this.updatedProperties.clear();
      this.updatedProperties.putAll(this.properties);
      Iterator var1 = this.blueprint.getAnimations().keySet().iterator();

      while(var1.hasNext()) {
         String name = (String)var1.next();
         IAnimationProperty property = (IAnimationProperty)this.updatedProperties.get(name);
         if (property != null && !property.update()) {
            this.updatedProperties.remove(name);
            this.forceStopAnimation(name);
         }
      }

   }

   public void updateBone(ModelBone bone) {
      bone.setHasGlobalRotation(false);
      KeyframeTypeRegistry registry = ModelEngineAPI.getAPI().getKeyframeTypeRegistry();
      Iterator var3 = registry.getKeys().iterator();

      while(true) {
         KeyframeType type;
         do {
            if (!var3.hasNext()) {
               return;
            }

            String typeName = (String)var3.next();
            type = (KeyframeType)registry.get(typeName);
         } while(type.isGlobal());

         IAnimationProperty property;
         for(Stack stack = this.getUpdateStack(type, bone); !stack.isEmpty(); type.updateBone(IPriorityHandler.class, this, bone, property)) {
            property = (IAnimationProperty)stack.pop();
            Timeline timeline = (Timeline)property.getBlueprintAnimation().getTimelines().get(bone.getBoneId());
            if (timeline != null && timeline.isGlobalRotation()) {
               bone.setHasGlobalRotation(true);
            }
         }
      }
   }

   public boolean hasFinishedAllAnimations() {
      Iterator var1 = this.properties.values().iterator();

      IAnimationProperty property;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         property = (IAnimationProperty)var1.next();
      } while(property.isFinished());

      return false;
   }

   public void setDefaultProperty(AnimationHandler.DefaultProperty defaultProperty) {
      this.defaultProperties.put(defaultProperty.getState(), defaultProperty);
   }

   public AnimationHandler.DefaultProperty getDefaultProperty(ModelState state) {
      return (AnimationHandler.DefaultProperty)this.defaultProperties.get(state);
   }

   private Stack<IAnimationProperty> getUpdateStack(KeyframeType<?, ?> type, ModelBone bone) {
      Stack<IAnimationProperty> stack = new Stack();
      String boneName = bone.getBoneId();
      Iterator var5 = this.blueprint.getAnimationDescendingPriority().iterator();

      while(var5.hasNext()) {
         String name = (String)var5.next();
         IAnimationProperty property = (IAnimationProperty)this.updatedProperties.get(name);
         if (property != null) {
            stack.push(property);
            if (property.isOverride() && property.containsKeyframe(type, boneName) && property.getPhase() == IAnimationProperty.Phase.PLAY) {
               break;
            }
         }
      }

      return stack;
   }

   public void tickGlobal() {
      Iterator var1 = this.blueprint.getAnimations().keySet().iterator();

      while(true) {
         IAnimationProperty property;
         do {
            if (!var1.hasNext()) {
               return;
            }

            String name = (String)var1.next();
            property = (IAnimationProperty)this.properties.get(name);
         } while(property == null);

         KeyframeTypeRegistry registry = ModelEngineAPI.getAPI().getKeyframeTypeRegistry();
         Iterator var5 = registry.getKeys().iterator();

         while(var5.hasNext()) {
            String typeName = (String)var5.next();
            KeyframeType<? extends AbstractKeyframe<?>, ?> type = (KeyframeType)registry.get(typeName);
            if (type.isGlobal()) {
               type.updateModel(IPriorityHandler.class, this, property);
            }
         }
      }
   }

   public void forEachProperty(BiConsumer<String, IAnimationProperty> consumer) {
      this.properties.forEach(consumer);
   }

   @Nullable
   public IAnimationProperty getAnimation(String animation) {
      return (IAnimationProperty)this.properties.get(animation);
   }

   public Map<String, IAnimationProperty> getAnimations() {
      return ImmutableMap.copyOf(this.properties);
   }

   @Nullable
   public IAnimationProperty playAnimation(String animation, double lerpIn, double lerpOut, double speed, boolean force) {
      BlueprintAnimation blueprintAnimation = (BlueprintAnimation)this.blueprint.getAnimations().get(animation);
      if (blueprintAnimation == null) {
         return null;
      } else {
         SimpleProperty property = new SimpleProperty(this.activeModel, blueprintAnimation, lerpIn, lerpOut, speed);
         return this.playAnimation(property, force) ? property : null;
      }
   }

   public boolean playAnimation(IAnimationProperty property, boolean force) {
      AnimationPlayEvent event = new AnimationPlayEvent(this.activeModel, property);
      ModelEngineAPI.callEvent(event);
      if (event.isCancelled()) {
         return false;
      } else {
         String name = property.getName();
         if (!this.properties.containsKey(name)) {
            this.properties.put(name, property);
            return true;
         } else {
            IAnimationProperty old = (IAnimationProperty)this.properties.get(name);
            if (!force && !old.canReplace()) {
               return false;
            } else {
               this.properties.put(name, property);
               return true;
            }
         }
      }
   }

   public void playState(ModelState state) {
      AnimationHandler.DefaultProperty defProperty = this.getDefaultProperty(state);
      IAnimationProperty property = this.firstSpawn ? defProperty.build(this.activeModel, 0.0D, defProperty.getLerpOut(), defProperty.getSpeed()) : defProperty.build(this.activeModel);
      if (property != null) {
         property.setForceLoopMode(state.getLoopMode());
         property.setForceOverride(state.isOverride());
         this.playAnimation(property, false);
      }

   }

   public boolean isPlayingAnimation(String animation) {
      return this.properties.containsKey(animation);
   }

   public void stopAnimation(String animation) {
      IAnimationProperty property = (IAnimationProperty)this.properties.get(animation);
      if (property != null) {
         if (property.getLerpOut() > 1.0E-5D) {
            property.stop();
         } else {
            this.forceStopAnimation(animation);
         }
      }

   }

   public void forceStopAnimation(String animation) {
      IAnimationProperty property = (IAnimationProperty)this.properties.remove(animation);
      if (property != null) {
         AnimationEndEvent event = new AnimationEndEvent(this.activeModel, property);
         ModelEngineAPI.callEvent(event);
      }

   }

   public void forceStopAllAnimations() {
      Iterator var1 = this.properties.values().iterator();

      while(var1.hasNext()) {
         IAnimationProperty property = (IAnimationProperty)var1.next();
         AnimationEndEvent event = new AnimationEndEvent(this.activeModel, property);
         ModelEngineAPI.callEvent(event);
      }

      this.properties.clear();
   }

   public void save(SavedData data) {
      IPriorityHandler.super.save(data);
      SavedData animationData = new SavedData();
      this.getAnimations().forEach((key, property) -> {
         property.save().ifPresent((propertyData) -> {
            animationData.putData(key, propertyData);
         });
      });
      data.putData("animations", animationData);
   }

   public void load(SavedData data) {
      IPriorityHandler.super.load(data);
      AnimationPropertyRegistry registry = ModelEngineAPI.getAnimationPropertyRegistry();
      data.getData("animations").ifPresent((animationData) -> {
         Iterator var3 = animationData.keySet().iterator();

         while(var3.hasNext()) {
            String key = (String)var3.next();
            animationData.getData(key).ifPresent((propertyData) -> {
               IAnimationProperty property = registry.createAnimationProperty(this, propertyData);
               this.playAnimation(property, true);
            });
         }

      });
   }

   public static PriorityHandler create(ActiveModel model, SavedData data) {
      PriorityHandler handler = new PriorityHandler(model);
      handler.load(data);
      return handler;
   }

   public ActiveModel getActiveModel() {
      return this.activeModel;
   }
}

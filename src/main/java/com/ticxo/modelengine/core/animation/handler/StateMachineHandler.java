package com.ticxo.modelengine.core.animation.handler;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.AnimationPropertyRegistry;
import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.ModelState;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.handler.IPriorityHandler;
import com.ticxo.modelengine.api.animation.handler.IStateMachineHandler;
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
import com.ticxo.modelengine.api.utils.data.io.DataIO;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.api.utils.state.StateMachine;
import com.ticxo.modelengine.api.utils.state.StateNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class StateMachineHandler implements IStateMachineHandler {
   private final ActiveModel activeModel;
   private final ModelBlueprint blueprint;
   private final Map<ModelState, AnimationHandler.DefaultProperty> defaultProperties = Maps.newConcurrentMap();
   private final TreeMap<Integer, StateMachineHandler.AnimationStateMachine> stateMachines = new TreeMap();
   private final Queue<Runnable> actionQueue = new ConcurrentLinkedQueue();
   private boolean firstSpawn = true;

   public StateMachineHandler(ActiveModel activeModel) {
      this.activeModel = activeModel;
      this.blueprint = activeModel.getBlueprint();
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.IDLE, 0.25D, 0.0D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.WALK, 0.25D, 0.0D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.STRAFE, 0.25D, 0.0D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.JUMP_START, 0.0D, 0.0D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.JUMP, 0.0D, 0.0D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.JUMP_END, 0.0D, 0.0D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.HOVER, 0.25D, 0.0D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.FLY, 0.25D, 0.0D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.SPAWN, 0.0D, 0.0D, 1.0D));
      this.setDefaultProperty(new AnimationHandler.DefaultProperty(ModelState.DEATH, 0.0D, 0.0D, 1.0D));
      this.configureAnimation();
   }

   private void configureAnimation() {
      StateMachineHandler.AnimationStateMachine stateMachine = new StateMachineHandler.AnimationStateMachine(false);
      StateNode<BaseEntity<?>> root = stateMachine.getRootNode();
      StateNode<BaseEntity<?>> spawn = stateMachine.createAnimationNode(() -> {
         return this.createStateProperty(ModelState.SPAWN);
      });
      StateNode<BaseEntity<?>> idle = stateMachine.createAnimationNode(() -> {
         return this.createStateProperty(ModelState.IDLE);
      });
      StateNode<BaseEntity<?>> walk = stateMachine.createAnimationNode(() -> {
         return this.createStateProperty(ModelState.WALK);
      });
      StateNode<BaseEntity<?>> strafe = stateMachine.createAnimationNode(() -> {
         return this.createStateProperty(ModelState.STRAFE);
      });
      StateNode<BaseEntity<?>> jumpStart = stateMachine.createAnimationNode(() -> {
         return this.createStateProperty(ModelState.JUMP_START);
      });
      StateNode<BaseEntity<?>> jumpLoop = stateMachine.createAnimationNode(() -> {
         return this.createStateProperty(ModelState.JUMP);
      });
      StateNode<BaseEntity<?>> jumpEnd = stateMachine.createAnimationNode(() -> {
         return this.createStateProperty(ModelState.JUMP_END);
      });
      StateNode<BaseEntity<?>> hover = stateMachine.createAnimationNode(() -> {
         return this.createStateProperty(ModelState.HOVER);
      });
      StateNode<BaseEntity<?>> fly = stateMachine.createAnimationNode(() -> {
         return this.createStateProperty(ModelState.FLY);
      });
      StateNode<BaseEntity<?>> death = stateMachine.createAnimationNode(() -> {
         this.forceStopAllAnimations();
         return this.createStateProperty(ModelState.DEATH);
      });
      root.addConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      root.addConnectedNode((baseEntity) -> {
         return this.hasAnimation(ModelState.SPAWN);
      }, spawn);
      root.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && baseEntity.isWalking() && this.hasAnimation(ModelState.FLY);
      }, fly);
      root.addConnectedNode((baseEntity) -> {
         return baseEntity.isFlying() && this.hasAnimation(ModelState.HOVER);
      }, hover);
      root.addConnectedNode((baseEntity) -> {
         return baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP_START);
      }, jumpStart);
      root.addConnectedNode((baseEntity) -> {
         return baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP);
      }, jumpLoop);
      root.addConnectedNode((baseEntity) -> {
         return baseEntity.isStrafing() && this.hasAnimation(ModelState.STRAFE);
      }, strafe);
      root.addConnectedNode(BaseEntity::isWalking, walk);
      root.addConnectedNode((baseEntity) -> {
         return !baseEntity.isWalking();
      }, idle);
      spawn.addConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      spawn.setCommonPredicate((baseEntity) -> {
         return stateMachine.hasFinishedPlaying(ModelState.SPAWN);
      });
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
         return baseEntity.isJumping() && this.hasAnimation(ModelState.JUMP);
      }, jumpLoop);
      spawn.addConnectedNode((baseEntity) -> {
         return baseEntity.isStrafing() && this.hasAnimation(ModelState.STRAFE);
      }, strafe);
      spawn.addConnectedNode(BaseEntity::isWalking, walk);
      spawn.addConnectedNode((baseEntity) -> {
         return !baseEntity.isWalking();
      }, idle);
      idle.addConnectedNode((baseEntity) -> {
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
      walk.addConnectedNode((baseEntity) -> {
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
      strafe.addConnectedNode((baseEntity) -> {
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
      jumpStart.addForceConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      jumpStart.setCommonPredicate((baseEntity) -> {
         return stateMachine.hasFinishedPlaying(ModelState.JUMP_START);
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
      jumpLoop.addConnectedNode((baseEntity) -> {
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
      jumpEnd.addForceConnectedNode((baseEntity) -> {
         return !baseEntity.isAlive();
      }, death);
      jumpEnd.setCommonPredicate((baseEntity) -> {
         return stateMachine.hasFinishedPlaying(ModelState.JUMP_END);
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
      fly.addConnectedNode((baseEntity) -> {
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
      this.stateMachines.put(0, stateMachine);
   }

   public void prepare() {
      while(!this.actionQueue.isEmpty()) {
         ((Runnable)this.actionQueue.poll()).run();
      }

      synchronized(this.stateMachines) {
         this.stateMachines.values().forEach((machine) -> {
            machine.execute(this.activeModel.getModeledEntity().getBase());
         });
      }

      this.firstSpawn = false;
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

         Stack stack = this.getUpdateStack(type, bone);

         while(!stack.isEmpty()) {
            StateMachineHandler.AnimationStateMachine stateMachine = (StateMachineHandler.AnimationStateMachine)stack.pop();
            IAnimationProperty currentProperty = stateMachine.currentAnimation;
            IAnimationProperty lastProperty = stateMachine.lastAnimation;
            type.updateBone(IStateMachineHandler.class, this, bone, currentProperty, lastProperty);
         }
      }
   }

   public boolean hasFinishedAllAnimations() {
      synchronized(this.stateMachines) {
         Iterator var2 = this.stateMachines.values().iterator();

         StateMachineHandler.AnimationStateMachine machine;
         do {
            if (!var2.hasNext()) {
               return true;
            }

            machine = (StateMachineHandler.AnimationStateMachine)var2.next();
         } while(machine.currentAnimation == null || machine.currentAnimation.isFinished());

         return false;
      }
   }

   public void setDefaultProperty(AnimationHandler.DefaultProperty defaultProperty) {
      this.defaultProperties.put(defaultProperty.getState(), defaultProperty);
   }

   public AnimationHandler.DefaultProperty getDefaultProperty(ModelState state) {
      return (AnimationHandler.DefaultProperty)this.defaultProperties.get(state);
   }

   public void tickGlobal() {
      synchronized(this.stateMachines) {
         Iterator var2 = this.stateMachines.values().iterator();

         while(true) {
            StateMachineHandler.AnimationStateMachine machine;
            do {
               if (!var2.hasNext()) {
                  return;
               }

               machine = (StateMachineHandler.AnimationStateMachine)var2.next();
            } while(machine.currentAnimation == null);

            KeyframeTypeRegistry registry = ModelEngineAPI.getAPI().getKeyframeTypeRegistry();
            Iterator var5 = registry.getKeys().iterator();

            while(var5.hasNext()) {
               String typeName = (String)var5.next();
               KeyframeType<? extends AbstractKeyframe<?>, ?> type = (KeyframeType)registry.get(typeName);
               if (type.isGlobal()) {
                  type.updateModel(IPriorityHandler.class, this, machine.currentAnimation);
               }
            }
         }
      }
   }

   @Nullable
   public IAnimationProperty playAnimation(String animation, double lerpIn, double lerpOut, double speed, boolean force) {
      return this.playAnimation(1, animation, lerpIn, lerpOut, speed, force);
   }

   public boolean playAnimation(IAnimationProperty property, boolean force) {
      return this.playAnimation(1, property, force);
   }

   private Stack<StateMachineHandler.AnimationStateMachine> getUpdateStack(KeyframeType<?, ?> type, ModelBone bone) {
      Stack<StateMachineHandler.AnimationStateMachine> stack = new Stack();
      if (this.stateMachines.isEmpty()) {
         return stack;
      } else {
         String boneName = bone.getBoneId();

         for(Entry entry = this.stateMachines.lastEntry(); entry != null; entry = this.stateMachines.lowerEntry((Integer)entry.getKey())) {
            StateMachineHandler.AnimationStateMachine machine = (StateMachineHandler.AnimationStateMachine)entry.getValue();
            IAnimationProperty currentProperty = machine.currentAnimation;
            IAnimationProperty lastProperty = machine.lastAnimation;
            if (currentProperty != null) {
               stack.push(machine);
               if (this.isLastProperty(currentProperty, type, boneName) && (lastProperty == null || this.isLastProperty(lastProperty, type, boneName))) {
                  break;
               }
            }
         }

         return stack;
      }
   }

   private boolean isLastProperty(IAnimationProperty property, KeyframeType<?, ?> type, String boneName) {
      return property.isOverride() && property.containsKeyframe(type, boneName) && property.getPhase() == IAnimationProperty.Phase.PLAY;
   }

   @Nullable
   public IAnimationProperty getAnimation(String animation) {
      synchronized(this.stateMachines) {
         Iterator var3 = this.stateMachines.values().iterator();

         StateMachineHandler.AnimationStateMachine machine;
         do {
            if (!var3.hasNext()) {
               return null;
            }

            machine = (StateMachineHandler.AnimationStateMachine)var3.next();
         } while(!machine.isPlaying(animation));

         return machine.currentAnimation;
      }
   }

   public Map<String, IAnimationProperty> getAnimations() {
      HashMap<String, IAnimationProperty> map = new HashMap();
      Iterator var2 = this.stateMachines.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<Integer, StateMachineHandler.AnimationStateMachine> entry = (Entry)var2.next();
         int id = (Integer)entry.getKey();
         StateMachineHandler.AnimationStateMachine stateMachine = (StateMachineHandler.AnimationStateMachine)entry.getValue();
         IAnimationProperty animation = stateMachine.getCurrentAnimation();
         if (animation != null) {
            map.put(id + ":" + animation.getName(), animation);
         }
      }

      return map;
   }

   @Nullable
   public IAnimationProperty getAnimation(int priority, String animation) {
      StateMachineHandler.AnimationStateMachine machine = (StateMachineHandler.AnimationStateMachine)this.stateMachines.get(priority);
      return machine != null && machine.isPlaying(animation) ? machine.currentAnimation : null;
   }

   @Nullable
   public IAnimationProperty playAnimation(int priority, String animation, double lerpIn, double lerpOut, double speed, boolean force) {
      BlueprintAnimation blueprintAnimation = (BlueprintAnimation)this.blueprint.getAnimations().get(animation);
      if (blueprintAnimation == null) {
         return null;
      } else {
         SimpleProperty property = new SimpleProperty(this.activeModel, blueprintAnimation, lerpIn, lerpOut, speed);
         return this.playAnimation(priority, property, force) ? property : null;
      }
   }

   public boolean playAnimation(int priority, IAnimationProperty property, boolean force) {
      AnimationPlayEvent event = new AnimationPlayEvent(this.activeModel, property);
      ModelEngineAPI.callEvent(event);
      if (event.isCancelled()) {
         return false;
      } else {
         synchronized(this.stateMachines) {
            StateMachineHandler.AnimationStateMachine machine = (StateMachineHandler.AnimationStateMachine)this.stateMachines.computeIfAbsent(priority, (id) -> {
               return new StateMachineHandler.AnimationStateMachine(true);
            });
            if (!force && machine.isPlaying(property.getName())) {
               return false;
            } else {
               StateNode<BaseEntity<?>> currentNode = machine.getCurrentNode();
               StateNode<BaseEntity<?>> node = machine.createAnimationNode(() -> {
                  return property;
               });
               node.addConnectedNode((baseEntity) -> {
                  return property.isEnded();
               }, machine.getRootNode());
               currentNode.addForceConnectedNode((baseEntity) -> {
                  return true;
               }, node);
               return true;
            }
         }
      }
   }

   public void refreshState(AnimationHandler.DefaultProperty property) {
      StateMachineHandler.AnimationStateMachine machine = (StateMachineHandler.AnimationStateMachine)this.stateMachines.get(0);
      if (machine != null && machine.isPlaying(property.getAnimation())) {
         machine.forceReentry(this.activeModel.getModeledEntity().getBase());
      }

   }

   public boolean isPlayingAnimation(String animation) {
      return this.getAnimation(animation) != null;
   }

   public boolean isPlayingAnimation(int priority, String animation) {
      return this.getAnimation(priority, animation) != null;
   }

   public void stopAnimation(String animation) {
      this.stateMachines.keySet().forEach((integer) -> {
         this.stopAnimation(integer, animation);
      });
   }

   public void stopAnimation(int priority, String animation) {
      StateMachineHandler.AnimationStateMachine machine = (StateMachineHandler.AnimationStateMachine)this.stateMachines.get(priority);
      if (machine != null && machine.isPlaying(animation)) {
         if (machine.currentAnimation.getLerpOut() > 1.0E-5D) {
            machine.currentAnimation.stop();
         } else {
            machine.getCurrentNode().addForceConnectedNode((baseEntity) -> {
               return true;
            }, machine.getRootNode());
         }
      }

   }

   public void forceStopAnimation(String animation) {
      this.stateMachines.keySet().forEach((integer) -> {
         this.forceStopAnimation(integer, animation);
      });
   }

   public void forceStopAnimation(int priority, String animation) {
      StateMachineHandler.AnimationStateMachine machine = (StateMachineHandler.AnimationStateMachine)this.stateMachines.get(priority);
      if (machine != null && machine.isPlaying(animation)) {
         machine.getCurrentNode().addForceConnectedNode((baseEntity) -> {
            return true;
         }, machine.getRootNode());
      }

   }

   public void forceStopAllAnimations() {
      this.actionQueue.add(() -> {
         StateMachineHandler.AnimationStateMachine defaultStateMachine = (StateMachineHandler.AnimationStateMachine)this.stateMachines.get(0);
         this.stateMachines.clear();
         if (defaultStateMachine != null) {
            this.stateMachines.put(0, defaultStateMachine);
         }

      });
   }

   private boolean hasAnimation(ModelState state) {
      return this.blueprint.getAnimations().containsKey(state.getString());
   }

   @Nullable
   private IAnimationProperty createStateProperty(ModelState state) {
      AnimationHandler.DefaultProperty defProperty = this.getDefaultProperty(state);
      IAnimationProperty property = this.firstSpawn ? defProperty.build(this.activeModel, 0.0D, defProperty.getLerpOut(), defProperty.getSpeed()) : defProperty.build(this.activeModel);
      if (property != null) {
         property.setForceLoopMode(state.getLoopMode());
         property.setForceOverride(state.isOverride());
      }

      return property;
   }

   public void save(SavedData data) {
      IStateMachineHandler.super.save(data);
      SavedData stateMachinesData = new SavedData();
      this.stateMachines.forEach((id, asm) -> {
         asm.save().ifPresent((smd) -> {
            stateMachinesData.putData(Integer.toString(id), smd);
         });
      });
      data.putData("state_machines", stateMachinesData);
   }

   public void load(SavedData data) {
      IStateMachineHandler.super.load(data);
      data.getData("state_machines").ifPresent((stateMachinesData) -> {
         Iterator var2 = stateMachinesData.keySet().iterator();

         while(var2.hasNext()) {
            String key = (String)var2.next();
            int id = Integer.parseInt(key);
            stateMachinesData.getData(key).ifPresent((smd) -> {
               StateMachineHandler.AnimationStateMachine asm = new StateMachineHandler.AnimationStateMachine(true);
               asm.load(smd);
               this.stateMachines.put(id, asm);
            });
         }

      });
   }

   public static StateMachineHandler create(ActiveModel model, SavedData data) {
      StateMachineHandler handler = new StateMachineHandler(model);
      handler.load(data);
      return handler;
   }

   public ActiveModel getActiveModel() {
      return this.activeModel;
   }

   public TreeMap<Integer, StateMachineHandler.AnimationStateMachine> getStateMachines() {
      return this.stateMachines;
   }

   public class AnimationStateMachine extends StateMachine<BaseEntity<?>> implements DataIO {
      protected final boolean saved;
      protected StateNode<BaseEntity<?>> rootNode;
      @Nullable
      protected IAnimationProperty lastAnimation;
      @Nullable
      protected IAnimationProperty currentAnimation;

      public StateNode<BaseEntity<?>> getCurrentNode() {
         return this.currentNode == null ? this.getRootNode() : this.currentNode;
      }

      public StateNode<BaseEntity<?>> getRootNode() {
         if (this.rootNode == null) {
            this.rootNode = new StateNode(this);
            this.rootNode.setEntryAction((baseEntity) -> {
               this.currentAnimation = null;
            });
            this.rootNode.setExitAction((baseEntity) -> {
               this.rootNode.clearForceConnectedNodes();
               this.rootNode.clearConnectedNodes();
               this.lastAnimation = this.currentAnimation;
            });
            this.setEntryNode(this.rootNode);
         }

         return this.rootNode;
      }

      public StateNode<BaseEntity<?>> createAnimationNode(Supplier<IAnimationProperty> propertySupplier) {
         StateNode<BaseEntity<?>> node = new StateNode(this);
         node.setEntryAction((baseEntity) -> {
            this.currentAnimation = (IAnimationProperty)propertySupplier.get();
         });
         node.setAction((baseEntity) -> {
            if (this.currentAnimation != null) {
               this.currentAnimation.update();
            }

         });
         node.setExitAction((baseEntity) -> {
            AnimationEndEvent event = new AnimationEndEvent(StateMachineHandler.this.activeModel, this.currentAnimation);
            ModelEngineAPI.callEvent(event);
            this.lastAnimation = this.currentAnimation;
         });
         return node;
      }

      public boolean hasFinishedPlaying(ModelState modelState) {
         return !this.isPlaying(StateMachineHandler.this.getDefaultProperty(modelState).getAnimation());
      }

      public boolean isPlaying(String animation) {
         boolean var10000;
         label32: {
            if (this.currentAnimation != null && this.currentAnimation.getName().equals(animation) && this.currentAnimation.getPhase() != IAnimationProperty.Phase.LERPOUT) {
               switch(this.currentAnimation.getLoopMode()) {
               case ONCE:
                  if (this.currentAnimation.getTime() < this.currentAnimation.getBlueprintAnimation().getLength()) {
                     break label32;
                  }
                  break;
               case LOOP:
                  if (this.currentAnimation.getTime() < this.currentAnimation.getBlueprintAnimation().getLength() + 0.05D) {
                     break label32;
                  }
                  break;
               default:
                  break label32;
               }
            }

            var10000 = false;
            return var10000;
         }

         var10000 = true;
         return var10000;
      }

      public boolean isPlayingOrEnding(String animation) {
         return this.currentAnimation != null && this.currentAnimation.getName().equals(animation) && !this.currentAnimation.isEnded();
      }

      public void forceReentry(BaseEntity<?> base) {
         this.getCurrentNode().acceptEntry(base);
      }

      public void save(SavedData data) {
         if (this.saved) {
            if (this.lastAnimation != null) {
               this.lastAnimation.save().ifPresent((data1) -> {
                  data.putData("last_animation", data1);
               });
            }

            if (this.currentAnimation != null) {
               this.currentAnimation.save().ifPresent((data1) -> {
                  data.putData("current_animation", data1);
               });
            }

         }
      }

      public void load(SavedData data) {
         AnimationPropertyRegistry registry = ModelEngineAPI.getAnimationPropertyRegistry();
         data.getData("last_animation").ifPresent((property) -> {
            this.lastAnimation = registry.createAnimationProperty(StateMachineHandler.this, property);
         });
         data.getData("current_animation").ifPresent((property) -> {
            this.currentAnimation = registry.createAnimationProperty(StateMachineHandler.this, property);
         });
         StateNode<BaseEntity<?>> currentNode = this.getCurrentNode();
         if (this.currentAnimation != null) {
            StateNode<BaseEntity<?>> node = this.createAnimationNode(() -> {
               return this.currentAnimation;
            });
            node.addConnectedNode((baseEntity) -> {
               return this.currentAnimation.isEnded();
            }, this.getRootNode());
            currentNode.addForceConnectedNode((baseEntity) -> {
               return true;
            }, node);
         }

      }

      public AnimationStateMachine(boolean saved) {
         this.saved = saved;
      }

      @Nullable
      public IAnimationProperty getLastAnimation() {
         return this.lastAnimation;
      }

      public void setLastAnimation(@Nullable IAnimationProperty lastAnimation) {
         this.lastAnimation = lastAnimation;
      }

      @Nullable
      public IAnimationProperty getCurrentAnimation() {
         return this.currentAnimation;
      }

      public void setCurrentAnimation(@Nullable IAnimationProperty currentAnimation) {
         this.currentAnimation = currentAnimation;
      }
   }
}

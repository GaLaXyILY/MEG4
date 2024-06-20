package com.ticxo.modelengine.api.utils.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class StateNode<T> {
   private final StateMachine<T> machine;
   private final Map<Predicate<T>, Function<T, StateNode<T>>> forceConnected = new LinkedHashMap();
   private final Map<Predicate<T>, Function<T, StateNode<T>>> connected = new LinkedHashMap();
   private Consumer<T> entryAction;
   private Consumer<T> action;
   private Consumer<T> exitAction;
   private Predicate<T> commonPredicate;

   public void addForceConnectedNode(Predicate<T> condition, StateNode<T> node) {
      this.forceConnected.put(condition, (t) -> {
         return node;
      });
   }

   public void addForceConnectedNode(Predicate<T> condition, Function<T, StateNode<T>> node) {
      this.forceConnected.put(condition, node);
   }

   public void clearForceConnectedNodes() {
      this.forceConnected.clear();
   }

   public void addConnectedNode(Predicate<T> condition, StateNode<T> node) {
      this.connected.put(condition, (t) -> {
         return node;
      });
   }

   public void addConnectedNode(Predicate<T> condition, Function<T, StateNode<T>> node) {
      this.connected.put(condition, node);
   }

   public void clearConnectedNodes() {
      this.connected.clear();
   }

   public void acceptAction(T target) {
      if (this.action != null) {
         this.action.accept(target);
      }

   }

   public void acceptEntry(T target) {
      if (this.entryAction != null) {
         this.entryAction.accept(target);
      }

   }

   public void acceptExit(T target) {
      if (this.exitAction != null) {
         this.exitAction.accept(target);
      }

   }

   public boolean testCommonPredicate(T target) {
      return this.commonPredicate == null || this.commonPredicate.test(target);
   }

   public StateNode(StateMachine<T> machine) {
      this.machine = machine;
   }

   public StateMachine<T> getMachine() {
      return this.machine;
   }

   public Map<Predicate<T>, Function<T, StateNode<T>>> getForceConnected() {
      return this.forceConnected;
   }

   public Map<Predicate<T>, Function<T, StateNode<T>>> getConnected() {
      return this.connected;
   }

   public Consumer<T> getEntryAction() {
      return this.entryAction;
   }

   public Consumer<T> getAction() {
      return this.action;
   }

   public Consumer<T> getExitAction() {
      return this.exitAction;
   }

   public Predicate<T> getCommonPredicate() {
      return this.commonPredicate;
   }

   public void setEntryAction(Consumer<T> entryAction) {
      this.entryAction = entryAction;
   }

   public void setAction(Consumer<T> action) {
      this.action = action;
   }

   public void setExitAction(Consumer<T> exitAction) {
      this.exitAction = exitAction;
   }

   public void setCommonPredicate(Predicate<T> commonPredicate) {
      this.commonPredicate = commonPredicate;
   }
}

package com.ticxo.modelengine.api.utils.state;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

public class StateMachine<T> {
   protected StateNode<T> currentNode;

   public void setEntryNode(StateNode<T> entryNode) {
      this.currentNode = entryNode;
   }

   public StateNode<T> createNode() {
      return new StateNode(this);
   }

   public void execute(T target) {
      boolean updated = false;
      Iterator var3 = this.currentNode.getForceConnected().entrySet().iterator();

      Entry entry;
      while(var3.hasNext()) {
         entry = (Entry)var3.next();
         if (((Predicate)entry.getKey()).test(target)) {
            this.currentNode.acceptExit(target);
            this.currentNode = (StateNode)((Function)entry.getValue()).apply(target);
            if (this.currentNode != null) {
               this.currentNode.acceptEntry(target);
            }

            updated = true;
            break;
         }
      }

      if (!updated && this.currentNode.testCommonPredicate(target)) {
         var3 = this.currentNode.getConnected().entrySet().iterator();

         while(var3.hasNext()) {
            entry = (Entry)var3.next();
            if (((Predicate)entry.getKey()).test(target)) {
               this.currentNode.acceptExit(target);
               this.currentNode = (StateNode)((Function)entry.getValue()).apply(target);
               if (this.currentNode != null) {
                  this.currentNode.acceptEntry(target);
               }
               break;
            }
         }
      }

      if (this.currentNode != null) {
         this.currentNode.acceptAction(target);
      }

   }

   public StateNode<T> getCurrentNode() {
      return this.currentNode;
   }
}

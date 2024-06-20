package com.ticxo.modelengine.api.nms.impl;

import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;

public class EmptyBodyRotationController implements BodyRotationController {
   public float getYHeadRot() {
      return 0.0F;
   }

   public void setYHeadRot(float rot) {
   }

   public float getXHeadRot() {
      return 0.0F;
   }

   public void setXHeadRot(float rot) {
   }

   public float getYBodyRot() {
      return 0.0F;
   }

   public void setYBodyRot(float rot) {
   }

   public boolean isHeadClampUneven() {
      return false;
   }

   public void setHeadClampUneven(boolean flag) {
   }

   public boolean isBodyClampUneven() {
      return false;
   }

   public void setBodyClampUneven(boolean flag) {
   }

   public float getMaxHeadAngle() {
      return 0.0F;
   }

   public void setMaxHeadAngle(float angle) {
   }

   public float getMaxBodyAngle() {
      return 0.0F;
   }

   public void setMaxBodyAngle(float angle) {
   }

   public float getMinHeadAngle() {
      return 0.0F;
   }

   public void setMinHeadAngle(float angle) {
   }

   public float getMinBodyAngle() {
      return 0.0F;
   }

   public void setMinBodyAngle(float angle) {
   }

   public float getStableAngle() {
      return 0.0F;
   }

   public void setStableAngle(float angle) {
   }

   public boolean isPlayerMode() {
      return false;
   }

   public void setPlayerMode(boolean flag) {
   }

   public int getRotationDelay() {
      return 0;
   }

   public void setRotationDelay(int delay) {
   }

   public int getRotationDuration() {
      return 0;
   }

   public void setRotationDuration(int duration) {
   }
}

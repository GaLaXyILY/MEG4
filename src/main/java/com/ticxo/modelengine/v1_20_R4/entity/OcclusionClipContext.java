package com.ticxo.modelengine.v1_20_R4.entity;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.RayTrace.BlockCollisionOption;
import net.minecraft.world.level.RayTrace.FluidCollisionOption;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class OcclusionClipContext extends RayTrace {
   public OcclusionClipContext(Vec3D start, Vec3D end) {
      super(start, end, BlockCollisionOption.c, FluidCollisionOption.a, (Entity)null);
   }

   public VoxelShape a(IBlockData state, IBlockAccess world, BlockPosition pos) {
      return state.p() && state.c(world, pos) == VoxelShapes.b() ? VoxelShapes.b() : VoxelShapes.a();
   }
}

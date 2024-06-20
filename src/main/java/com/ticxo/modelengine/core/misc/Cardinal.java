package com.ticxo.modelengine.core.misc;

import org.apache.commons.lang3.tuple.Triple;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public enum Cardinal {
   Q_000(0, 0, 0),
   Q_001(0, 0, 1),
   Q_002(0, 0, 2),
   Q_003(0, 0, 3),
   Q_010(0, 1, 0),
   Q_011(0, 1, 1),
   Q_012(0, 1, 2),
   Q_013(0, 1, 3),
   Q_020(0, 2, 0),
   Q_021(0, 2, 1),
   Q_022(0, 2, 2),
   Q_023(0, 2, 3),
   Q_030(0, 3, 0),
   Q_031(0, 3, 1),
   Q_032(0, 3, 2),
   Q_033(0, 3, 3),
   Q_100(1, 0, 0),
   Q_101(1, 0, 1),
   Q_102(1, 0, 2),
   Q_103(1, 0, 3),
   Q_120(1, 2, 0),
   Q_121(1, 2, 1),
   Q_122(1, 2, 2),
   Q_123(1, 2, 3);

   public final Quaternionf rotation;
   public final Quaternionf inverse;

   private Cardinal(int x, int y, int z) {
      this.rotation = (new Quaternionf()).rotationXYZ((float)x * 1.5707964F, (float)y * 1.5707964F, (float)z * 1.5707964F);
      this.inverse = this.rotation.invert(new Quaternionf());
   }

   private Triple<Quaternionf, Vector3f, Quaternionf> warpTransform(Quaternionf left, Vector3f scale, Quaternionf right) {
      Quaternionf nLeft = left.mul(this.rotation, new Quaternionf());
      Vector3f nScale = scale.rotate(this.inverse, new Vector3f());
      nScale.x = Math.signum(scale.x) * Math.abs(nScale.x);
      nScale.y = Math.signum(scale.y) * Math.abs(nScale.y);
      nScale.z = Math.signum(scale.z) * Math.abs(nScale.z);
      Quaternionf nRight = right.premul(this.inverse, new Quaternionf());
      return Triple.of(nLeft, nScale, nRight);
   }

   // $FF: synthetic method
   private static Cardinal[] $values() {
      return new Cardinal[]{Q_000, Q_001, Q_002, Q_003, Q_010, Q_011, Q_012, Q_013, Q_020, Q_021, Q_022, Q_023, Q_030, Q_031, Q_032, Q_033, Q_100, Q_101, Q_102, Q_103, Q_120, Q_121, Q_122, Q_123};
   }
}

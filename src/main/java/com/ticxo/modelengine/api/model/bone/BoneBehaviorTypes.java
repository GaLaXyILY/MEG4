package com.ticxo.modelengine.api.model.bone;

import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.type.Ghost;
import com.ticxo.modelengine.api.model.bone.type.Head;
import com.ticxo.modelengine.api.model.bone.type.HeldItem;
import com.ticxo.modelengine.api.model.bone.type.Leash;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.model.bone.type.NameTag;
import com.ticxo.modelengine.api.model.bone.type.PlayerLimb;
import com.ticxo.modelengine.api.model.bone.type.Segment;
import com.ticxo.modelengine.api.model.bone.type.SubHitbox;

public final class BoneBehaviorTypes {
   public static BoneBehaviorType<? extends Head> HEAD;
   public static BoneBehaviorType<? extends Ghost> GHOST;
   public static BoneBehaviorType<? extends Mount> MOUNT;
   public static BoneBehaviorType<? extends SubHitbox> SUB_HITBOX;
   public static BoneBehaviorType<? extends NameTag> NAMETAG;
   public static BoneBehaviorType<? extends HeldItem> ITEM;
   public static BoneBehaviorType<? extends Segment> SEGMENT;
   public static BoneBehaviorType<? extends Leash> LEASH;
   public static BoneBehaviorType<? extends PlayerLimb> PLAYER_LIMB;
}

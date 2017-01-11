package com.dmonsters.entity;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import javax.annotation.Nullable;

import com.dmonsters.ai.EntityAIDrowning;
import com.dmonsters.ai.EntityAIFreezerAttack;
import com.dmonsters.ai.EntityAIMutantSteveAttack;
import com.dmonsters.ai.MakeEnviroFreezed;
import com.dmonsters.main.MainMod;
import com.dmonsters.main.ModConfig;
import com.dmonsters.main.ModSounds;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityBoat;

public class EntityFreezer extends EntityMob {

    public EntityFreezer(World worldIn) {
        super(worldIn);
        setSize(0.9F, 1.95F);
        initEntityAI();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.13D * ModConfig.speedMultiplier * ModConfig.freezerSpeedMultiplier);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(16.0D * ModConfig.strengthMultiplier * ModConfig.freezerStrengthMultiplier);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(45.0D * ModConfig.healthMultiplier * ModConfig.freezerHealthMultiplier);
    }
    
    @Override
    public void onUpdate() {
    	super.onUpdate();
    	spawnParticle();
    	dealFreezDamage();
    }
    
    public void onLivingUpdate() {
        if (this.worldObj.isDaytime() && !this.worldObj.isRemote) {
            float f = this.getBrightness(1.0F);
            BlockPos blockpos = new BlockPos(this.posX, (double)Math.round(this.posY), this.posZ);
            if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.worldObj.canSeeSky(blockpos)) {
            	this.setFire(8);
            }
        }
        super.onLivingUpdate();
    }
    
    private int AABBckeckTicks = 0;
	private void dealFreezDamage() {
		if (!this.worldObj.isRemote) {
			AABBckeckTicks++;
			if (AABBckeckTicks < 40)
				return;
			AABBckeckTicks = 0;
			BlockPos AABB_01 = new BlockPos(this.posX - 4, this.posY, this.posZ - 4);
			BlockPos AABB_02 = new BlockPos(this.posX + 4, this.posY + 4, this.posZ + 4);
			AxisAlignedBB AABB = new AxisAlignedBB(AABB_01, AABB_02);
			List<Entity> entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, AABB);
			for (int i = 0; i < entities.size(); i++) {
				Entity entity = entities.get(i);
				if (entity instanceof EntityPlayer) {
					entity.attackEntityFrom(DamageSource.generic, 1);
				} else {
					if (entity instanceof EntityLiving) {
						if (((EntityLiving) entity).getCreatureAttribute() != EnumCreatureAttribute.UNDEAD) {
							entity.attackEntityFrom(DamageSource.generic, 1);
						}
					}
				}
			}
		}
	}
    
	private void spawnParticle() {
		double motionX = rand.nextGaussian() * 0.15D;
		double motionY = rand.nextGaussian() * 0.15D;
		double motionZ = rand.nextGaussian() * 0.15D;
		worldObj.spawnParticle(EnumParticleTypes.SNOW_SHOVEL,
				posX + rand.nextFloat() * width * 2.0F - width, 
				posY + 0.5D + rand.nextFloat() * height, 
				posZ + rand.nextFloat() * width * 2.0F - width,
				motionX,
				motionY,
				motionZ,
				new int[0]);
   }

    protected void initEntityAI() {
    	this.tasks.addTask(0, new MakeEnviroFreezed(this));
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIFreezerAttack(this, 1.0D, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
    	this.applyEntityAI();
    }

    private void applyEntityAI() {
    	this.targetTasks.addTask(0, new EntityAINearestAttackableTarget(this, EntityPlayer.class, false));
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (super.attackEntityAsMob(entityIn)) {
        	((EntityLivingBase)entityIn).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 600));
        	this.playSound("dmonsters:mob.freezer.attack", 1, 1);
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    protected String getDeathSound()
    {
		return "dmonsters:mob.freezer.death";
    }
    
    @Override
    protected String getLivingSound()
    {
    	return "dmonsters:mob.freezer.idle";
    }

    @Override
    protected String getHurtSound()
    {
    	return "dmonsters:mob.freezer.hurt";
    }
    
    public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    protected boolean isValidLightLevel() {
        return true;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }
}

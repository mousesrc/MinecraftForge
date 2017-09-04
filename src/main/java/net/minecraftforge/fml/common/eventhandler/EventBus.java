/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.common.eventhandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.stats.AchievementList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.brewing.PlayerBrewedPotionEvent;
import net.minecraftforge.event.brewing.PotionBrewEvent;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;
import net.minecraftforge.event.entity.minecart.MinecartEvent;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import net.minecraftforge.fml.common.gameevent.*;
import org.apache.logging.log4j.Level;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;
import org.lwjgl.opencl.CL;
import scala.tools.cmd.gen.AnyVals;

public class EventBus implements IEventExceptionHandler
{
    private static int maxID = 0;

    private ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = new ConcurrentHashMap<Object, ArrayList<IEventListener>>();
    private Map<Object,ModContainer> listenerOwners = new MapMaker().weakKeys().weakValues().makeMap();
    private final int busID = maxID++;
    private IEventExceptionHandler exceptionHandler;

    public EventBus()
    {
        ListenerList.resize(busID + 1);
        exceptionHandler = this;
    }

    public EventBus(@Nonnull IEventExceptionHandler handler)
    {
        this();
        Preconditions.checkNotNull(handler, "EventBus exception handler can not be null");
        exceptionHandler = handler;
    }

    public void register(Object target)
    {
        if (listeners.containsKey(target))
        {
            return;
        }

        ModContainer activeModContainer = Loader.instance().activeModContainer();
        if (activeModContainer == null)
        {
            FMLLog.log.error("Unable to determine registrant mod for {}. This is a critical error and should be impossible", target, new Throwable());
            activeModContainer = Loader.instance().getMinecraftModContainer();
        }
        listenerOwners.put(target, activeModContainer);
        boolean isStatic = target.getClass() == Class.class;
        @SuppressWarnings("unchecked")
        Set<? extends Class<?>> supers = isStatic ? Sets.newHashSet((Class<?>)target) : TypeToken.of(target.getClass()).getTypes().rawTypes();
        for (Method method : (isStatic ? (Class<?>)target : target.getClass()).getMethods())
        {
            if (isStatic && !Modifier.isStatic(method.getModifiers()))
                continue;
            else if (!isStatic && Modifier.isStatic(method.getModifiers()))
                continue;

            for (Class<?> cls : supers)
            {
                try
                {
                    Method real = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
                    if (real.isAnnotationPresent(SubscribeEvent.class))
                    {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length != 1)
                        {
                            throw new IllegalArgumentException(
                                "Method " + method + " has @SubscribeEvent annotation, but requires " + parameterTypes.length +
                                " arguments.  Event handler methods must require a single argument."
                            );
                        }

                        Class<?> eventType = parameterTypes[0];

                        if (!Event.class.isAssignableFrom(eventType))
                        {
                            throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
                        }

                        register(eventType, target, real, activeModContainer);
                        break;
                    }
                }
                catch (NoSuchMethodException e)
                {
                    ;
                }
            }
        }
    }

    private void register(Class<?> eventType, Object target, Method method, final ModContainer owner)
    {
        try
        {
            Constructor<?> ctr = eventType.getConstructor();
            ctr.setAccessible(true);
            Event event = (Event)ctr.newInstance();
            final ASMEventHandler asm = new ASMEventHandler(target, method, owner, IGenericEvent.class.isAssignableFrom(eventType));

            IEventListener listener = asm;
            if (IContextSetter.class.isAssignableFrom(eventType))
            {
                listener = new IEventListener()
                {
                    @Override
                    public void invoke(Event event)
                    {
                        ModContainer old = Loader.instance().activeModContainer();
                        Loader.instance().setActiveModContainer(owner);
                        asm.invoke(event);
                        Loader.instance().setActiveModContainer(old);
                    }
                };
            }

            event.getListenerList().register(busID, asm.getPriority(), listener);

            ArrayList<IEventListener> others = listeners.get(target);
            if (others == null)
            {
                others = new ArrayList<IEventListener>();
                listeners.put(target, others);
            }
            others.add(listener);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void unregister(Object object)
    {
        ArrayList<IEventListener> list = listeners.remove(object);
        if(list == null)
            return;
        for (IEventListener listener : list)
        {
            ListenerList.unregisterAll(busID, listener);
        }
    }

    public boolean post(Event event)
    {
        //MCPCRevive start
        boolean bukkit_called = false;
        org.bukkit.event.Event thisevent = null;
        if(event instanceof net.minecraftforge.event.entity.player.PlayerEvent)
        {
            if (event instanceof PlayerWakeUpEvent) {
                EntityPlayer mp = ((PlayerWakeUpEvent) event).getEntityPlayer();
                // CraftBukkit start - fire PlayerBedLeaveEvent
                if (mp.getBukkitEntity() instanceof Player) {
                    Player player = (Player) mp.getBukkitEntity();

                    org.bukkit.block.Block bed;
                    BlockPos blockposition = mp.bedLocation;
                    if (blockposition != null) {
                        bed = player.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                    } else {
                        bed = player.getWorld().getBlockAt(player.getLocation());
                    }
                    thisevent = new PlayerBedLeaveEvent(player, bed);
                }
                // CraftBukkit end
            }
            else if (event instanceof PlayerInteractEvent.EntityInteractSpecific) {
                Vector vec = new Vector();
                Vec3d v = ((PlayerInteractEvent.EntityInteractSpecific) event).getTarget().getPositionVector();
                vec.setX(v.xCoord);
                vec.setY(v.yCoord);
                vec.setZ(v.zCoord);
                thisevent = new org.bukkit.event.player.PlayerInteractAtEntityEvent((Player) ((PlayerInteractEvent.EntityInteractSpecific) event).getEntityPlayer().getBukkitEntity(),((PlayerInteractEvent.EntityInteractSpecific) event).getTarget().getBukkitEntity(),vec);
            }
            else if (event instanceof PlayerInteractEvent.RightClickBlock) {
                // CraftBukkit start
                EntityPlayer player = ((PlayerInteractEvent.RightClickBlock) event).getEntityPlayer();
                Vec3d hitpos = ((PlayerInteractEvent.RightClickBlock) event).getHitVec();
                if(hitpos == null)
                {
                    hitpos = new Vec3d(player.posX,player.posY,player.posZ);
                }
                BlockPos blockPos = new BlockPos(player.posX + hitpos.xCoord, player.posY + hitpos.yCoord, player.posZ + hitpos.zCoord);
                EnumFacing direction = ((PlayerInteractEvent.RightClickBlock) event).getFace();
                net.minecraft.item.ItemStack stack = ((PlayerInteractEvent.RightClickBlock) event).getItemStack();
                thisevent = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, blockPos, direction, player.inventory.getCurrentItem(), EnumHand.MAIN_HAND);
                bukkit_called = true;
                //CraftBukkit end
            }
            else if(event instanceof PlayerInteractEvent.RightClickEmpty)
            {
                // CraftBukkit start
                EntityPlayer player = ((PlayerInteractEvent.RightClickBlock) event).getEntityPlayer();
                Vec3d hitpos = ((PlayerInteractEvent.RightClickBlock) event).getHitVec();
                BlockPos blockPos = new BlockPos(player.posX + hitpos.xCoord, player.posY + hitpos.yCoord, player.posZ + hitpos.zCoord);
                EnumFacing direction = ((PlayerInteractEvent.RightClickBlock) event).getFace();
                net.minecraft.item.ItemStack stack = ((PlayerInteractEvent.RightClickBlock) event).getItemStack();
                thisevent = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, blockPos, direction, player.inventory.getCurrentItem(), EnumHand.MAIN_HAND);
                bukkit_called = true;
                //CraftBukkit end
            }
            else if (event instanceof PlayerInteractEvent.EntityInteract) {
                Vector vec = new Vector();
                Vec3d v = ((PlayerInteractEvent.EntityInteract) event).getTarget().getPositionVector();
                vec.setX(v.xCoord);
                vec.setY(v.yCoord);
                vec.setZ(v.zCoord);
                thisevent = new org.bukkit.event.player.PlayerInteractAtEntityEvent((Player) ((PlayerInteractEvent.EntityInteract) event).getEntityPlayer().getBukkitEntity(),((PlayerInteractEvent.EntityInteract) event).getTarget().getBukkitEntity(),vec);
            }
            else if(event instanceof PlayerInteractEvent.LeftClickEmpty)
            {
                // CraftBukkit start
                EntityPlayer player = ((PlayerInteractEvent.LeftClickBlock) event).getEntityPlayer();
                Vec3d hitpos = ((PlayerInteractEvent.LeftClickBlock) event).getHitVec();
                BlockPos blockPos = new BlockPos(player.posX + hitpos.xCoord, player.posY + hitpos.yCoord, player.posZ + hitpos.zCoord);
                EnumFacing direction = ((PlayerInteractEvent.LeftClickBlock) event).getFace();
                net.minecraft.item.ItemStack stack = ((PlayerInteractEvent.LeftClickBlock) event).getItemStack();
                thisevent = CraftEventFactory.callPlayerInteractEvent(player, Action.LEFT_CLICK_AIR, blockPos, direction, player.inventory.getCurrentItem(), EnumHand.MAIN_HAND);
                bukkit_called = true;
                //CraftBukkit end

            }
            else if (event instanceof PlayerInteractEvent.LeftClickBlock) {
                // CraftBukkit start
                EntityPlayer player = ((PlayerInteractEvent.LeftClickBlock) event).getEntityPlayer();
                Vec3d hitpos = ((PlayerInteractEvent.LeftClickBlock) event).getHitVec();
                BlockPos blockPos = new BlockPos(player.posX + hitpos.xCoord, player.posY + hitpos.yCoord, player.posZ + hitpos.zCoord);
                EnumFacing direction = ((PlayerInteractEvent.LeftClickBlock) event).getFace();
                net.minecraft.item.ItemStack stack = ((PlayerInteractEvent.LeftClickBlock) event).getItemStack();
                thisevent = CraftEventFactory.callPlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, blockPos, direction, player.inventory.getCurrentItem(), EnumHand.MAIN_HAND);
                bukkit_called = true;
                //CraftBukkit end
            }
            else if(event instanceof PlayerInteractEvent.RightClickItem)
            {

            }
            else if (event instanceof AchievementEvent) {
                net.minecraft.stats.Achievement thisAchievement = ((AchievementEvent) event).getAchievement();
                Achievement bukkit = null;
                if(thisAchievement == AchievementList.ACQUIRE_IRON)
                {
                    bukkit = Achievement.ACQUIRE_IRON;
                }
                else if(thisAchievement == AchievementList.BAKE_CAKE)
                {
                    bukkit = Achievement.BAKE_CAKE;
                }
                else if(thisAchievement == AchievementList.BLAZE_ROD)
                {
                    bukkit = Achievement.GET_BLAZE_ROD;
                }
                else if(thisAchievement == AchievementList.BOOKCASE)
                {
                    bukkit = Achievement.BOOKCASE;
                }
                else if(thisAchievement == AchievementList.BREED_COW)
                {
                    bukkit = Achievement.BREED_COW;
                }
                else if(thisAchievement == AchievementList.BUILD_BETTER_PICKAXE)
                {
                    bukkit = Achievement.BUILD_BETTER_PICKAXE;
                }
                else if(thisAchievement == AchievementList.BUILD_FURNACE)
                {
                    bukkit = Achievement.BUILD_FURNACE;
                }
                else if(thisAchievement == AchievementList.BUILD_HOE)
                {
                    bukkit = Achievement.BUILD_HOE;
                }
                else if(thisAchievement == AchievementList.BUILD_PICKAXE)
                {
                    bukkit = Achievement.BUILD_PICKAXE;
                }
                else if(thisAchievement == AchievementList.BUILD_WORK_BENCH)
                {
                    bukkit = Achievement.BUILD_WORKBENCH;
                }
                else if(thisAchievement == AchievementList.COOK_FISH)
                {
                    bukkit = Achievement.COOK_FISH;
                }
                else if(thisAchievement == AchievementList.DIAMONDS)
                {
                    bukkit = Achievement.GET_DIAMONDS;
                }
                else if(thisAchievement == AchievementList.DIAMONDS_TO_YOU)
                {
                    bukkit = Achievement.DIAMONDS_TO_YOU;
                }
                else if(thisAchievement == AchievementList.EXPLORE_ALL_BIOMES)
                {
                    bukkit = Achievement.EXPLORE_ALL_BIOMES;
                }
                else if(thisAchievement == AchievementList.FLY_PIG)
                {
                    bukkit = Achievement.FLY_PIG;
                }
                else if(thisAchievement == AchievementList.ENCHANTMENTS)
                {
                    bukkit = Achievement.ENCHANTMENTS;
                }
                else if(thisAchievement == AchievementList.BUILD_SWORD)
                {
                    bukkit = Achievement.BUILD_SWORD;
                }
                else if(thisAchievement == AchievementList.FULL_BEACON)
                {
                    bukkit = Achievement.FULL_BEACON;
                }
                else if(thisAchievement == AchievementList.KILL_ENEMY)
                {
                    bukkit = Achievement.KILL_ENEMY;
                }
                else if(thisAchievement == AchievementList.KILL_WITHER)
                {
                    bukkit = Achievement.KILL_WITHER;
                }
                else if(thisAchievement == AchievementList.KILL_COW)
                {
                    bukkit = Achievement.KILL_COW;
                }
                else if(thisAchievement == AchievementList.OVERKILL)
                {
                    bukkit = Achievement.OVERKILL;
                }
                else if(thisAchievement == AchievementList.PORTAL)
                {
                    bukkit = Achievement.NETHER_PORTAL;
                }
                else if(thisAchievement == AchievementList.THE_END)
                {
                    bukkit = Achievement.END_PORTAL;
                }
                else if(thisAchievement == AchievementList.THE_END2)
                {
                    bukkit = Achievement.THE_END;
                }
                else if(thisAchievement == AchievementList.GHAST)
                {
                    bukkit = Achievement.GHAST_RETURN;
                }
                else if(thisAchievement == AchievementList.SNIPE_SKELETON)
                {
                    bukkit = Achievement.SNIPE_SKELETON;
                }
                else if(thisAchievement == AchievementList.POTION)
                {
                    bukkit = Achievement.BREW_POTION;
                }
                else if(thisAchievement == AchievementList.MAKE_BREAD)
                {
                    bukkit = Achievement.MAKE_BREAD;
                }
                else if(thisAchievement == AchievementList.MINE_WOOD)
                {
                    bukkit = Achievement.MINE_WOOD;
                }
                else if(thisAchievement == AchievementList.ON_A_RAIL)
                {
                    bukkit = Achievement.ON_A_RAIL;
                }
                else if(thisAchievement == AchievementList.OPEN_INVENTORY)
                {
                    bukkit = Achievement.OPEN_INVENTORY;
                }
                else if(thisAchievement == AchievementList.OVERPOWERED)
                {
                    bukkit = Achievement.OVERPOWERED;
                }
                else if(thisAchievement == AchievementList.SPAWN_WITHER)
                {
                    bukkit = Achievement.SPAWN_WITHER;
                }
                else
                {
                    //TODO: GIVE EXTRA ENUM OF MOD_ACHIEVEMENT
                }
                thisevent = new PlayerAchievementAwardedEvent((Player) ((AchievementEvent) event).getEntityPlayer().getBukkitEntity(), bukkit);
            } else if (event instanceof PlayerSleepInBedEvent) {
                EntityPlayer mp = ((PlayerSleepInBedEvent) event).getEntityPlayer();
                BlockPos bedLocation = ((PlayerSleepInBedEvent) event).getPos();
                // CraftBukkit start - fire PlayerBedEnterEvent
                if (mp.getBukkitEntity() instanceof Player) {
                    Player player = (Player) mp.getBukkitEntity();
                    org.bukkit.block.Block mybed = player.getWorld().getBlockAt(bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());

                    thisevent = new PlayerBedEnterEvent(player, mybed);
                }
                // CraftBukkit end

            }
            else if (event instanceof PlayerSetSpawnEvent) {
                World world = ((PlayerSetSpawnEvent) event).getEntityPlayer().getEntityWorld();
                BlockPos newspawn = ((PlayerSetSpawnEvent) event).getNewSpawn();
                thisevent = new SpawnChangeEvent(world.getWorld(),new Location(world.getWorld(),newspawn.getX(),newspawn.getY(),newspawn.getZ()));

            }
            else if (event instanceof PlayerDestroyItemEvent) {
                CraftEventFactory.callPlayerItemBreakEvent(((PlayerDestroyItemEvent) event).getEntityPlayer(),((PlayerDestroyItemEvent) event).getOriginal());
                bukkit_called = true;
            }
        }
        else if(event instanceof net.minecraftforge.fml.common.gameevent.PlayerEvent)
        {
            if(event instanceof net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent)
            {
                net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event1 = (net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent) event;
                EntityPlayerMP player = (EntityPlayerMP) event1.player;
                InetSocketAddress address = (InetSocketAddress) player.connection.netManager.getRemoteAddress();
                String hostname = address.getHostName();
                thisevent = new PlayerLoginEvent((Player)player.getBukkitEntity(),hostname,address.getAddress());
            }
            else if(event instanceof net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent)
            {
                net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event1 = (net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent) event;
                thisevent = new PlayerQuitEvent((Player) event1.player.getBukkitEntity(),event1.toString());
            }
            else if(event instanceof net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent)
            {
                net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event1 = (net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent) event;
                EntityPlayerMP p = (EntityPlayerMP) event1.player;
                Location loc = new Location(p.getServerWorld().getWorld(),p.posX,p.posY,p.posZ);
                boolean isBedspawn = p.getBedLocation(p.getServerWorld().provider.getDimension()) != null;
                thisevent = new PlayerRespawnEvent((Player) p.getBukkitEntity(),loc,isBedspawn);
            }
            else if(event instanceof net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent)
            {
                net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event1 = (net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent) event;
                thisevent = new PlayerChangedWorldEvent((Player) event1.player.getBukkitEntity(), DimensionManager.getWorld(event1.fromDim).getWorld());
            }
        }
        else if(event instanceof BlockEvent)
        {
            if(event instanceof BlockEvent.BreakEvent)
            {
                BlockEvent.BreakEvent event1 = (BlockEvent.BreakEvent) event;
                thisevent = new BlockBreakEvent(event1.getWorld().getWorld().getBlockAt(event1.getPos().getX(),event1.getPos().getY(),event1.getPos().getZ()), (Player) event1.getPlayer().getBukkitEntity());
            }
            else if(event instanceof BlockEvent.HarvestDropsEvent)
            {
                BlockEvent.HarvestDropsEvent event1 = (BlockEvent.HarvestDropsEvent) event;
                EntityPlayer harvester = ((BlockEvent.HarvestDropsEvent) event).getHarvester();
                if(harvester != null) {
                    thisevent = new BlockBreakEvent(event1.getWorld().getWorld().getBlockAt(event1.getPos().getX(), event1.getPos().getY(), event1.getPos().getZ()), (Player) event1.getHarvester().getBukkitEntity());
                }
            }
            else if(event instanceof BlockEvent.CropGrowEvent.Post)
            {
                IBlockState last = ((BlockEvent.CropGrowEvent.Post) event).getOriginalState();
                IBlockState current = ((BlockEvent.CropGrowEvent.Post) event).getState();
                World world = ((BlockEvent.CropGrowEvent.Post) event).getWorld();
                BlockPos blockposition = ((BlockEvent.CropGrowEvent.Post) event).getPos();
                Block farmblock = last.getBlock();
                org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(),blockposition.getZ());
                CraftBlockState state = (CraftBlockState) block.getState();
                state.setTypeId(net.minecraft.block.Block.getIdFromBlock(farmblock));
                state.setRawData((byte) farmblock.getMetaFromState(last));
                thisevent = new BlockGrowEvent(block, state);
                // Block pos = ((BlockEvent.CropGrowEvent) event).getPos();
               // event.
               // thisevent = new BlockGrowEvent()
            }

            //NeighborNotigy
            //NoteBlock
            //CreateFluidSourceEvent
        }
        else if(event instanceof WorldEvent)
        {
            if(event instanceof WorldEvent.Load)
            {
                thisevent = new org.bukkit.event.world.WorldLoadEvent(((WorldEvent.Load) event).getWorld().getWorld());
            }
            else if(event instanceof WorldEvent.CreateSpawnPosition)
            {

            }
            else if(event instanceof WorldEvent.Unload)
            {
                thisevent = new org.bukkit.event.world.WorldUnloadEvent(((WorldEvent.Unload) event).getWorld().getWorld());
            }
            else if(event instanceof ChunkEvent)
            {
                if(event instanceof ChunkEvent.Load)
                {
                    Chunk c =((ChunkEvent.Load) event).getChunk();
                    thisevent = new org.bukkit.event.world.ChunkLoadEvent(new CraftChunk(c),c.isEmpty());
                }
                else if(event instanceof ChunkEvent.Unload)
                {
                    Chunk c =((ChunkEvent.Unload) event).getChunk();
                    thisevent = new ChunkUnloadEvent(new CraftChunk(c));
                }
            }
            if(event instanceof SaplingGrowTreeEvent)
            {
                //no thing
            }
            if(event instanceof WorldEvent.Save)
            {
                thisevent = new WorldSaveEvent(((WorldEvent.Save) event).getWorld().getWorld());
            }
        }
        else if(event instanceof ServerChatEvent)
        {
            thisevent = new org.bukkit.event.player.AsyncPlayerChatEvent(false, (Player) ((ServerChatEvent) event).getPlayer().getBukkitEntity(),((ServerChatEvent) event).getMessage(),new LazyPlayerSet(((ServerChatEvent) event).getPlayer().getServer()));
        }
        else if(event instanceof PotionBrewEvent.Post)
        {
            //moved to TileEntityBrewingStand
        }
        else if(event instanceof ExplosionEvent)
        {
            //moved to World
        }
        else if(event instanceof PopulateChunkEvent.Post)
        {
            CraftChunk chunk = new CraftChunk(((PopulateChunkEvent.Post) event).getWorld().getChunkFromChunkCoords(((PopulateChunkEvent.Post) event).getChunkX(),((PopulateChunkEvent.Post) event).getChunkZ()));
            thisevent = new ChunkPopulateEvent(chunk);
        }
        else if(event instanceof EntityEvent) {
            if (event instanceof EntityTravelToDimensionEvent) {

            } else if (event instanceof MinecartEvent) {
                if (event instanceof MinecartInteractEvent) {
                    EntityPlayer player = ((MinecartInteractEvent) event).getPlayer();
                    EnumHand hand = ((MinecartInteractEvent) event).getHand();
                    EntityMinecart minecart = ((MinecartInteractEvent) event).getMinecart();
                    thisevent = new org.bukkit.event.player.PlayerInteractEntityEvent((Player) player.getBukkitEntity(),minecart.getBukkitEntity());
                } else if (event instanceof MinecartUpdateEvent) {

                } else if (event instanceof MinecartCollisionEvent) {

                }
            } else if (event instanceof EntityEvent.EnteringChunk) {

            } else if (event instanceof EntityEvent.EntityConstructing) {

            } else if (event instanceof EntityStruckByLightningEvent) {

            } else if (event instanceof EntityMountEvent) {

            } else if (event instanceof EntityJoinWorldEvent) {

            } else if (event instanceof LivingEvent) {
                if (event instanceof LivingHurtEvent) {
                    double amount = ((LivingHurtEvent) event).getAmount();
                    DamageSource source = ((LivingHurtEvent) event).getSource();
                    Entity src = ((LivingHurtEvent) event).getEntityLiving();
                    EntityDamageEvent.DamageCause cause = null;
                    thisevent = new EntityDamageEvent(src.getBukkitEntity(), DamageSource.asBukkit(source),amount);
                }
                else if (event instanceof LivingHealEvent) {
                    float amount = ((LivingHealEvent) event).getAmount();
                    EntityLivingBase e = ((LivingHealEvent) event).getEntityLiving();
                    thisevent = new EntityRegainHealthEvent(e.getBukkitEntity(),amount, EntityRegainHealthEvent.RegainReason.REGEN);

                } else if (event instanceof LootingLevelEvent) {

                } else if (event instanceof LivingEquipmentChangeEvent) {
                    //
                } else if (event instanceof EnderTeleportEvent) {
                    Entity teleportingEntity = (Entity) ((EnderTeleportEvent) event).getEntityLiving();
                    BlockPos old = teleportingEntity.getPosition();
                    BlockPos target = new BlockPos(((EnderTeleportEvent) event).getTargetX(),((EnderTeleportEvent) event).getTargetY(),((EnderTeleportEvent) event).getTargetZ());
                    CraftWorld w = teleportingEntity.getEntityWorld().getWorld();
                    if(teleportingEntity instanceof Player)
                    {
                        thisevent = new PlayerTeleportEvent((Player) teleportingEntity.getBukkitEntity(),new Location(w,old.getX(),old.getY(),old.getZ()),new Location(w,target.getX(),target.getY(),target.getZ()), PlayerTeleportEvent.TeleportCause.ENDER_PEARL);

                    }
                    else
                    {
                        thisevent = new EntityTeleportEvent(teleportingEntity.getBukkitEntity(),new Location(w,old.getX(),old.getY(),old.getZ()),new Location(w,target.getX(),target.getY(),target.getZ()));
                    }
                } else if (event instanceof LivingDestroyBlockEvent) {

                } else if (event instanceof LivingEvent.LivingUpdateEvent) {

                } else if (event instanceof LivingExperienceDropEvent) {

                } else if (event instanceof LivingDeathEvent) {

                    //thisevent = new EntityDeathEvent();

                } else if (event instanceof LivingAttackEvent) {

                } else if (event instanceof AnimalTameEvent) {
                    thisevent = org.bukkit.craftbukkit.event.CraftEventFactory.callEntityTameEvent(((AnimalTameEvent) event).getAnimal(), ((AnimalTameEvent) event).getTamer());
                } else if (event instanceof LivingFallEvent) {

                } else if (event instanceof LivingEntityUseItemEvent) {

                } else if (event instanceof LivingEvent.LivingJumpEvent) {

                } else if (event instanceof LivingSpawnEvent) {

                } else if (event instanceof LivingSetAttackTargetEvent) {


                } else if (event instanceof LivingDropsEvent) {
                    EntityLivingBase dead = ((LivingDropsEvent) event).getEntityLiving();
                    List<EntityItem> items = ((LivingDropsEvent) event).getDrops();
                    List<org.bukkit.inventory.ItemStack> stacks = new ArrayList<org.bukkit.inventory.ItemStack>();
                    for (EntityItem item : items)
                    {
                        stacks.add(CraftItemStack.asBukkitCopy(item.getEntityItem()));
                    }
                    thisevent = new EntityDeathEvent((LivingEntity) dead.getBukkitEntity(),stacks);
                }

            } else if (event instanceof ZombieEvent) {

            } else if (event instanceof ItemEvent) {

            } else if (event instanceof ThrowableImpactEvent)
            {
                Entity projectile = ((ThrowableImpactEvent) event).getEntityThrowable();
                thisevent = new ProjectileHitEvent((Projectile) projectile.getBukkitEntity());
            }
        }
        if(thisevent != null)
        {
            Bukkit.getServer().getPluginManager().callEvent(thisevent);
            if(event.isCancelable())
            {
                if(thisevent instanceof org.bukkit.event.Cancellable)
                {
                    org.bukkit.event.Cancellable cancelable = (Cancellable)thisevent;
                    if(cancelable.isCancelled())
                    {
                        event.setCanceled(true);
                        return true;
                    }
                }
            }
        }
        //MCPCRevive end
        IEventListener[] listeners = event.getListenerList().getListeners(busID);
        int index = 0;
        try
        {
            for (; index < listeners.length; index++)
            {
                listeners[index].invoke(event);
            }
        }
        catch (Throwable throwable)
        {
            exceptionHandler.handleException(this, event, listeners, index, throwable);
            Throwables.propagate(throwable);
        }
        return (event.isCancelable() ? event.isCanceled() : false);
    }

    @Override
    public void handleException(EventBus bus, Event event, IEventListener[] listeners, int index, Throwable throwable)
    {
        FMLLog.log.error("Exception caught during firing event {}:", event, throwable);
        FMLLog.log.error("Index: {} Listeners:", index);
        for (int x = 0; x < listeners.length; x++)
        {
            FMLLog.log.error("{}: {}", x, listeners[x]);
        }
    }
}

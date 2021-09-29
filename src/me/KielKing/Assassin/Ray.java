package me.KielKing.Assassin;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
 * @author Unknown
 * Helper class for ray tracing
 */
public class Ray{
    Vector origin, direction;

    Ray(Vector origin, Vector direction){
        this.origin = origin;
        this.direction = direction;
    }

    public static Ray from(Player player){
        return new Ray(player.getEyeLocation().toVector(), player.getLocation().getDirection());
    }

    public static boolean intersects(Vector position, Vector min, Vector max){
        if(position.getX() < min.getX() || position.getX() > max.getX()){
            return false;
        }else if(position.getY() < min.getY() || position.getY() > max.getY()){
            return false;
        }else return !(position.getZ() < min.getZ()) && !(position.getZ() > max.getZ());
    }

    public Vector getPostion(double blocksAway){
        return origin.clone().add(direction.clone().multiply(blocksAway));
    }

    public boolean isOnLine(Vector position){
        double t = (position.getX() - origin.getX()) / direction.getX();
        return position.getBlockY() == origin.getY() + (t * direction.getY()) && position.getBlockZ() == origin.getZ() + (t * direction.getZ());
    }

    public ArrayList<Vector> traverse(double blocksAway, double accuracy){
        ArrayList<Vector> positions = new ArrayList<>();
        for(double d = 0; d <= blocksAway; d += accuracy){
            positions.add(getPostion(d));
        }
        return positions;
    }

    public Vector positionOfIntersection(Vector min, Vector max, double blocksAway, double accuracy){
        ArrayList<Vector> positions = traverse(blocksAway, accuracy);
        for(Vector position : positions){
            if(intersects(position, min, max)){
                return position;
            }
        }
        return null;
    }

    public boolean intersects(Vector min, Vector max, double blocksAway, double accuracy){
        ArrayList<Vector> positions = traverse(blocksAway, accuracy);
        for(Vector position : positions){
            if(intersects(position, min, max)){
                return true;
            }
        }
        return false;
    }

    public Vector positionOfIntersection(BoundingBox boundingBox, double blocksAway, double accuracy){
        ArrayList<Vector> positions = traverse(blocksAway, accuracy);
        for(Vector position : positions){
            if(intersects(position, boundingBox.min, boundingBox.max)){
                return position;
            }
        }
        return null;
    }

    public boolean intersects(BoundingBox boundingBox, double blocksAway, double accuracy){
        ArrayList<Vector> positions = traverse(blocksAway, accuracy);
        for(Vector position : positions){
            if(intersects(position, boundingBox.min, boundingBox.max)){
                return true;
            }
        }
        return false;
    }

    public void highlight(World world, double blocksAway, double accuracy){
        ArrayList<Vector> traverses = traverse(blocksAway, accuracy);
        traverses.remove(0);
        for(Vector position : traverses){
            Location location = position.toLocation(world);
            world.spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(), 5, 0.01, 0.01, 0.01, 10, new Particle.DustOptions(Color.RED, 1));
        }
    }

    public double direction(int i){
        switch(i){
            case 0:
                return direction.getX();
            case 1:
                return direction.getY();
            case 2:
                return direction.getZ();
            default:
                return 0;
        }
    }

    public double origin(int i){
        switch(i){
            case 0:
                return origin.getX();
            case 1:
                return origin.getY();
            case 2:
                return origin.getZ();
            default:
                return 0;
        }
    }
}
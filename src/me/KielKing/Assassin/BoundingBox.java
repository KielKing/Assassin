package me.KielKing.Assassin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BoundingBox{

    public final Vector min, max;

    public BoundingBox(Vector min, Vector max){
        this(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2){
        this.min = new Vector(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
        this.max = new Vector(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    private BoundingBox(Player player){
        this.min = getMin(player);
        this.max = getMax(player);
    }

    public static BoundingBox from(Player player){
        return new BoundingBox(player);
    }

    private Vector getMin(Player player){
        return player.getLocation().toVector().add(new Vector(-0.35, 0, -0.35));
    }

    private Vector getMax(Player player){
        return player.getLocation().toVector().add(new Vector(0.35, 1.9, 0.35));
    }

    public Vector getMin(){
        return min;
    }

    public Vector getMax(){
        return max;
    }

    public double min(int i){
        switch(i){
            case 0:
                return min.getX();
            case 1:
                return min.getY();
            case 2:
                return min.getZ();
            default:
                return 0;
        }
    }

    public double max(int i){
        switch(i){
            case 0:
                return max.getX();
            case 1:
                return max.getY();
            case 2:
                return max.getZ();
            default:
                return 0;
        }
    }

    public boolean collides(Ray ray, double tmin, double tmax){
        for(int i = 0; i < 3; i++){
            double d = 1 / ray.direction(i);
            double t0 = (min(i) - ray.origin(i)) * d;
            double t1 = (max(i) - ray.origin(i)) * d;
            if(d < 0){
                double t = t0;
                t0 = t1;
                t1 = t;
            }
            tmin = Math.max(t0, tmin);
            tmax = Math.min(t1, tmax);
            if(tmax <= tmin) return false;
        }
        return true;
    }

    public double collidesD(Ray ray, double tmin, double tmax){
        for(int i = 0; i < 3; i++){
            double d = 1 / ray.direction(i);
            double t0 = (min(i) - ray.origin(i)) * d;
            double t1 = (max(i) - ray.origin(i)) * d;
            if(d < 0){
                double t = t0;
                t0 = t1;
                t1 = t;
            }
            tmin = Math.max(t0, tmin);
            tmax = Math.min(t1, tmax);
            if(tmax <= tmin) return -1;
        }
        return tmin;
    }

    public boolean contains(Location location){
        if(location.getX() > max.getX()) return false;
        if(location.getY() > max.getY()) return false;
        if(location.getZ() > max.getZ()) return false;
        if(location.getX() < min.getX()) return false;
        if(location.getY() < min.getY()) return false;
        return !(location.getZ() < min.getZ());
    }
}

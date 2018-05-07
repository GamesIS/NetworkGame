package ru.ilku0917.networkGame;

import java.io.Serializable;

/**
 * для справки
 * https://habrahabr.ru/post/131931/
 */
public class Vector3 implements Serializable {
    private String name = "default";

    public double x;
    public double y;
    public double z;

    public Vector3() {
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vector3 v) { //Установка значений с помощью вектора
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public void set(double x, double y, double z) { //Установка значений с помощью координат
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void rotateZ(double angle) { //Вращение по Z
        double nx = x * Math.cos(angle) - y * Math.sin(angle);
        double ny = x * Math.sin(angle) + y * Math.cos(angle);
        double nz = z;
        set(nx, ny, nz);
    }

    public void rotateY(double angle) { //Вращение по Y
        double nz = z * Math.cos(angle) - x * Math.sin(angle);
        double nx = z * Math.sin(angle) + x * Math.cos(angle);
        double ny = y;
        set(nx, ny, nz);
    }

    public void rotateX(double angle) { //Вращение по Y
        double ny = y * Math.cos(angle) - z * Math.sin(angle);
        double nz = y * Math.sin(angle) + z * Math.cos(angle);
        double nx = x;
        set(nx, ny, nz);
    }

    public void translate(double x, double y, double z) {
        set(this.x + x, this.y + y, this.z + z);
    }

    public double getSize() {
        return Math.sqrt(x * x + y * y + z * z);
    } // Длина вектора

    public void normalize() { //Нормализация
        double size = getSize();
        x /= size;
        y /= size;
        z /= size;
    }

    /**
     * Умножение вектора на скаляр
     * Когда мы говорим о векторах, мы называем отдельные числа скалярами.
     * Например (3, 4) — вектор, а 5 — это скаляр.
     * В играх, часто бывает нужно умножить вектор на число (скаляр).
     * Например, моделируя простое сопротивление воздуха путём умножения скорости игрока на 0.9 в каждом кадре.
     * Чтобы сделать это, нам надо умножить каждый компонент вектора на скаляр.
     * Если скорость игрока (10, 20), то новая скорость будет:
     * 0.9*(10, 20) = (0.9 * 10, 0.9 * 20) = (9, 18).
     */
    public void scale(double f) { //Умножение вектора на скаляр
        x *= f;
        y *= f;
        z *= f;
    }

    public void add(Vector3 v) { //Сложение векторов
        x += v.x;
        y += v.y;
        z += v.z;
    }

    public void sub(Vector3 v) { // Вычитание векторов
        x -= v.x;
        y -= v.y;
        z -= v.z;
    }

    public double dot(Vector3 v) {
        return x * v.x + y * v.y + z * v.z;
    } //Скалярное произведение векторов

    public static double angleBetween(Vector3 a, Vector3 b) {
        double am = a.getSize();
        double bm = b.getSize();
        return Math.acos(a.dot(b) / (am * bm));
    }

    public static void sub(Vector3 r, Vector3 a, Vector3 b) {
        r.x = a.x - b.x;
        r.y = a.y - b.y;
        r.z = a.z - b.z;
    }

    public static void cross(Vector3 res, Vector3 left, Vector3 right) {
        double x = left.y * right.z - left.z * right.y;
        double y = right.x * left.z - right.z * left.x;
        double z = left.x * right.y - left.y * right.x;
        res.set(x, y, z);
    }

    public double getRelativeAngleBetween(Vector3 v) {//Угол между векторами http://www.webmath.ru/poleznoe/formules_4_7.php
        return getSign(v) * Math.acos(dot(v) / (getSize() * v.getSize()));
    }

    // http://www.oocities.org/pcgpe/math2d.html
    // http://gamedev.stackexchange.com/questions/45412/understanding-math-used-to-determine-if-vector-is-clockwise-counterclockwise-f
    public int getSign(Vector3 v) {
        return (y * v.x > x * v.y) ? -1 : 1;
    }

    public static double angle(double x1, double y1, double x2, double y2) {

        final double delta = (x1 * x2 + y1 * y2) / Math.sqrt(
                (x1 * x1 + y1 * y1) * (x2 * x2 + y2 * y2));

        if (delta > 1.0) {
            return 0.0;
        }
        if (delta < -1.0) {
            return 180.0;
        }

        return Math.toDegrees(Math.acos(delta));
    }

    @Override
    public String toString() {
        return "Vector3{" +
                "name='" + name + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}

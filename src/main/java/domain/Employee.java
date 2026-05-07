package domain;

public class Employee {
    private int id;
    private String name;
    private double salary;
    private int departmentId;

    public Employee(int id, String name, double salary, int departmentId) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.departmentId = departmentId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getSalary() { return salary; }
    public int getDepartmentId() { return departmentId; }
}
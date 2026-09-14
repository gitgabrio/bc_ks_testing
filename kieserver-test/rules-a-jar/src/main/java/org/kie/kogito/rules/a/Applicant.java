package org.kie.kogito.rules.a;

import java.io.Serializable;

/**
 * Applicant domain object.
 */
public class Applicant implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private int age;

    public Applicant() {}

    public Applicant(String id, int age) {
        this.id  = id;
        this.age = age;
    }

    public String getId()          { return id; }
    public void   setId(String id) { this.id = id; }

    public int  getAge()           { return age; }
    public void setAge(int age)    { this.age = age; }

    @Override
    public String toString() {
        return "Applicant{id='" + id + "', age=" + age + "}";
    }
}

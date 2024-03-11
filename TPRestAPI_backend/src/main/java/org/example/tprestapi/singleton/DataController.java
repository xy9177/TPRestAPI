package org.example.tprestapi.singleton;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DataController {
    private static DataController singleInstance = null;
    public EntityManager manager = null;
    private DataController(){
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("quiz_unit");
        this.manager = factory.createEntityManager();
    }

    public static DataController getSingleInstance() {
        if (singleInstance == null) {
            singleInstance = new DataController();
        }
        return singleInstance;
    }
}

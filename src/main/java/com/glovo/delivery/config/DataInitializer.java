package com.glovo.delivery.config;

import com.glovo.delivery.model.Courier;
import com.glovo.delivery.model.Point;
import com.glovo.delivery.model.enums.CourierType;
import com.glovo.delivery.repository.CourierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CourierRepository courierRepository;

    public DataInitializer(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing test couriers...");

        courierRepository.save(new Courier(new Point(10, 10), CourierType.PEDESTRIAN));
        courierRepository.save(new Courier(new Point(25, 30), CourierType.BICYCLE));
        courierRepository.save(new Courier(new Point(50, 50), CourierType.CAR));
        courierRepository.save(new Courier(new Point(80, 20), CourierType.BICYCLE));
        courierRepository.save(new Courier(new Point(15, 75), CourierType.CAR));
        courierRepository.save(new Courier(new Point(60, 90), CourierType.PEDESTRIAN));
        courierRepository.save(new Courier(new Point(35, 45), CourierType.CAR));
        courierRepository.save(new Courier(new Point(70, 65), CourierType.BICYCLE));

        log.info("Initialized {} test couriers", courierRepository.count());
        courierRepository.findAll().forEach(c ->
                log.info("  Courier {} [{}] at {}", c.getId(), c.getType(), c.getCurrentLocation()));
    }
}

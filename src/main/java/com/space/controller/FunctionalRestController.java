package com.space.controller;

import com.space.model.Ship;
import com.space.repository.ShipRepo;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "rest")
public class FunctionalRestController {
    @Autowired
    private ShipService shipService;

    @Autowired
    ShipRepo shipRepo;

    @GetMapping("/ships")
    @ResponseStatus(HttpStatus.OK)
    public List<Ship> getShips(@RequestParam Map<String, String> map) {
        return shipService.getShipsPage(map);
    }

    @PostMapping("/ships")
    public Ship createShip(@RequestBody Ship ship, HttpServletResponse response) {

        Ship ship1 = shipService.createShip(ship);
        if (ship1 == null) {
            response.setStatus(400);
        } else {
            response.setStatus(200);
        }

        return ship1;
    }

    @PostMapping("/ships/{id}")
    public Ship updateShip(@RequestBody Ship ship, @PathVariable Long id, HttpServletResponse response) {
        System.out.println("hi");
        ship.setId(id);
        Ship ship1 = shipService.updateShip(ship);

        if (ship1 != null && ship1.getId().equals(-1L)) {
            response.setStatus(400);
        } else if (ship1 == null) {
            response.setStatus(404);
        } else {
            response.setStatus(200);
        }

        return ship1;
    }

    @GetMapping("/ships/{id}")
    public Ship shipById(@PathVariable Long id, HttpServletResponse response) {
        Ship ship = shipService.getAllById(id);

        if (ship != null && ship.getId().equals(-1L)) {
            response.setStatus(400);
        } else if (ship == null) {
            response.setStatus(404);
        } else {
            response.setStatus(200);
        }
        return ship;
    }

    @DeleteMapping("/ships/{id}")
    public void deleteShip(@PathVariable Long id, HttpServletResponse response) {
        response.setStatus(shipService.deleteShip(id));
    }

    @GetMapping("/ships/count")
    @ResponseStatus(HttpStatus.OK)
    public Integer getCount(@RequestParam Map<String, String> map) {
        return shipService.getCountShips(map);
    }


    @GetMapping("/hello")
    public String getTest() {

        return "hello";
    }
}

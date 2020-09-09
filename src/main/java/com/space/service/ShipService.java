package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShipService {
    @Autowired
    private ShipRepo shipRepo;

    public List<Ship> getShipsPage(Map<String, String> map) {
        String pageNumberString = map.get("pageNumber");
        String pageSizeString = map.get("pageSize");
        String shipOrderString = map.get("order");
        ShipOrder shipOrder = ShipOrder.ID;
        Integer pageNumber = 0;
        Integer pageSize = 3;

        if (pageNumberString != null) {
            pageNumber = Integer.valueOf(pageNumberString);
        }

        if (pageSizeString != null) {
            pageSize = Integer.valueOf(pageSizeString);
        }

        if (shipOrderString != null) {
            shipOrder = ShipOrder.valueOf(shipOrderString);
        }

        List<Ship> list = getShipsHelp(map);
        Integer firstObject = pageNumber * pageSize;
        int lastIndex = Math.min(list.size(), firstObject + pageSize);

        if (shipOrder.equals(ShipOrder.SPEED)) {
            list = list.stream()
                    .sorted(Comparator.comparing(Ship::getSpeed)).collect(Collectors.toList());
        } else if (shipOrder.equals(ShipOrder.DATE)) {
            list = list.stream()
                    .sorted(Comparator.comparing(Ship::getProdDate)).collect(Collectors.toList());
        } else if (shipOrder.equals(ShipOrder.RATING)) {
            list = list.stream()
                    .sorted(Comparator.comparing(Ship::getRating)).collect(Collectors.toList());
        }
        return list.subList(firstObject, lastIndex);
    }

    public Ship updateShip(Ship ship) {
        Ship oldShip = new Ship();
        oldShip.setId(-1L);
        int year = 0;

        if (ship.getProdDate() != null) {
            year = getYear(ship.getProdDate());
        }
        if (ship.getName() != null && (ship.getName().length() > 50 || ship.getName().equals(""))) {
            return oldShip;
        } else if (ship.getPlanet() != null && (ship.getPlanet().length() > 50 || ship.getPlanet().equals(""))) {
            return oldShip;
        } else if (ship.getSpeed() != null && (ship.getSpeed() > 0.99 || ship.getSpeed() < 0.01)) {
            return oldShip;
        } else if (ship.getCrewSize() != null && (ship.getCrewSize() > 9999 || ship.getCrewSize() < 1)) {
            return oldShip;
        } else if (ship.getProdDate() != null && (ship.getProdDate().getTime() < 0 || year > 3019 || year < 2800)) {
            return oldShip;
        }

        oldShip = getAllById(ship.getId());


        if (oldShip == null || oldShip.getId() == -1L) {
            return oldShip;
        }

        boolean isEditSpeed = false;
        boolean isEditIsUsed = false;
        boolean isEditDate = false;

        if (ship.getUsed() != null) {
            oldShip.setUsed(ship.getUsed());
            isEditIsUsed = true;
        }
        if (ship.getSpeed() != null) {
            oldShip.setSpeed(ship.getSpeed());
            isEditSpeed = true;
        }
        if (ship.getCrewSize() != null) {
            oldShip.setCrewSize(ship.getCrewSize());
        }
        if (ship.getProdDate() != null) {
            oldShip.setProdDate(ship.getProdDate());
            isEditDate = true;
        }
        if (ship.getShipType() != null) {
            oldShip.setShipType(ship.getShipType());
        }
        if (ship.getPlanet() != null) {
            oldShip.setPlanet(ship.getPlanet());
        }
        if (ship.getName() != null) {
            oldShip.setName(ship.getName());
        }

        if (isEditDate || isEditIsUsed || isEditSpeed) {
            System.out.println(oldShip.getRating());
            oldShip.setRating(calculateRating(oldShip.getSpeed(), oldShip.getUsed(), oldShip.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear()));
            System.out.println(oldShip.getRating());
        }

        shipRepo.save(oldShip);
        return oldShip;
    }

    public Ship getAllById(Long id) {
        Ship ship = new Ship();

        if (id <= 0) {
            ship.setId(-1L);
            return ship;
        }

        return shipRepo.findAllById(id);
    }

    private int getYear(Date prodDate) {
        LocalDate localDate = prodDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return localDate.getYear();
    }

    public Ship createShip(Ship ship) {
        String name = ship.getName();
        String planet = ship.getPlanet();
        ShipType shipType = ship.getShipType();
        Date prodDate = ship.getProdDate();
        int year = 0;
        if (prodDate != null) {
            year = getYear(prodDate);
        }
        Boolean isUsed = false;
        if (ship.getUsed() != null) {
            isUsed = ship.getUsed();
        }
        ship.setUsed(isUsed);
        Double speed = ship.getSpeed();
        Integer crewSize = ship.getCrewSize();
        if (name == null || name.length() > 50 || name.equals("")) {
            return null;
        } else if (planet == null || planet.length() > 50 || planet.equals("")) {
            return null;
        } else if (speed == null || speed > 0.99 || speed < 0.01) {
            return null;
        } else if (crewSize == null || crewSize > 9999 || crewSize < 1) {
            return null;
        } else if (prodDate == null || prodDate.getTime() < 0 || year > 3019 || year < 2800) {
            return null;
        } else if (shipType == null) {
            return null;
        }
        Double rating = calculateRating(speed, isUsed, year);
        ship.setRating(rating);
        Long id = getMaxId();
        ship.setId(++id);
        shipRepo.save(ship);
        return ship;
    }

    private Double calculateRating(Double speed, Boolean isUsed, int year) {
        double k = (double) 0;

        if (isUsed) {
            k = 0.5;
        } else {
            k = 1.0;
        }

        double rating = (80 * speed * k) / (3019 - year + 1);

        return (double) Math.round(rating * 100d) / 100d;
    }

    private Long getMaxId() {
        Iterable<Ship> list = shipRepo.findAll();
        Long id = 0L;
        for (Ship shId : list) {
            if (shId.getId() > id) {
                id = shId.getId();
            }
        }
        return id;
    }

    public Integer getCountShips(Map<String, String> map) {
        return getShipsHelp(map).size();
    }

    private List<Ship> getShipsHelp(Map<String, String> map) {
        Iterable<Ship> ships = shipRepo.findAll();
        List<Ship> list = new ArrayList<>();
        String name = map.get("name");
        String planet = map.get("planet");
        String shipType = map.get("shipType");
        String after = map.get("after");
        String before = map.get("before");
        String isUsed = map.get("isUsed");
        String minSpeed = map.get("minSpeed");
        String maxSpeed = map.get("maxSpeed");
        String minCrewSize = map.get("minCrewSize");
        String maxCrewSize = map.get("maxCrewSize");
        String minRating = map.get("minRating");
        String maxRating = map.get("maxRating");

        for (Ship ship : ships) {

            if (name != null) {
                if (!ship.getName().contains(name)) {
                    continue;
                }
            }
            if (planet != null) {
                if (!ship.getPlanet().contains(planet)) {
                    continue;
                }
            }
            if (shipType != null && ship.getShipType() != ShipType.valueOf(shipType)) {
                continue;
            }
            if (after != null && before != null && (ship.getProdDate().getTime() > Long.parseLong(before) || ship.getProdDate().getTime() < Long.parseLong(after))) {
                continue;
            }
            if (isUsed != null && ship.getUsed() != Boolean.valueOf(isUsed)) {
                continue;
            }

            if (minSpeed == null) {
                if (maxSpeed != null && ship.getSpeed() > Double.parseDouble(maxSpeed)) {
                    continue;
                }
            } else if (maxSpeed == null) {
                if (ship.getSpeed() < Double.parseDouble(minSpeed)) {
                    continue;
                }
            } else if (ship.getSpeed() < Double.parseDouble(minSpeed) || ship.getSpeed() > Double.parseDouble(maxSpeed)) {
                continue;
            }

            if (minCrewSize == null) {
                if (maxCrewSize != null && ship.getCrewSize() > Integer.parseInt(maxCrewSize)) {
                    continue;
                }
            } else if (maxCrewSize == null) {
                if (ship.getCrewSize() < Integer.parseInt(minCrewSize)) {
                    continue;
                }
            } else if (ship.getCrewSize() < Integer.parseInt(minCrewSize) || ship.getCrewSize() > Integer.parseInt(maxCrewSize)) {
                continue;
            }

            if (minRating == null) {
                if (maxRating != null && ship.getRating() > Double.parseDouble(maxRating)) {
                    continue;
                }
            } else if (maxRating == null) {
                if (minCrewSize != null && (ship.getRating() < Double.parseDouble(minRating))) {
                    continue;
                }
            } else if (ship.getRating() < Double.parseDouble(minRating) || ship.getRating() > Double.parseDouble(maxRating)) {
                continue;
            }
            list.add(ship);

        }
        return list;

    }

    public int deleteShip(Long id) {
        if (id <= 0 || id > getMaxId()) {
            return 404;
        }
        Ship ship = shipRepo.findAllById(id);
        if (ship == null) {
            return 400;
        }
        shipRepo.deleteById(id);

        return 200;
    }

}
package com.example.myapplication.utils;


public class MapResult {
    public Result result;  // 修改这里，原来是resultMap
    public String msg;
    public String status;

    public String getFormatted_address() {
        return result.formatted_address;
    }

    public MapResult() {
    }

    public Result getResult() {  // 修改getter和setter方法名称
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

class Result {  // 之前是ResultMap
    public String formatted_address;
    public Location location;
    public AddressComponent addressComponent;

    public Result() {
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public AddressComponent getAddressComponent() {
        return addressComponent;
    }

    public void setAddressComponent(AddressComponent addressComponent) {
        this.addressComponent = addressComponent;
    }
}

class Location {
    public double lon;
    public double lat;
}

class AddressComponent {
    String address;
    String city;
    String road;
    String poi_position;
    String address_position;
    int road_distance;
    String poi;
    String poi_distance;
    int address_distance;
}

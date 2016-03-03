function AndroidMock() {}

AndroidMock.prototype.getWorkoutDataOn = function(timemillis) {
	return "[{\n" +
			"            \"value\": 300,\n" +
			"            \"color\":\"#F7464A\",\n" +
			"            \"highlight\": \"#FF5A5E\",\n" +
			"            \"label\": \"Walking\"\n" +
			"        }\n" +
			"]";
}

var Android = new AndroidMock();
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

var testTodayJson = 
                "[\n" +
                "        {\n" +
                "            \"value\": 300,\n" +
                "            \"color\":\"#F7464A\",\n" +
                "            \"highlight\": \"#FF5A5E\",\n" +
                "            \"label\": \"Walking\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"value\": 50,\n" +
                "            \"color\": \"#46BFBD\",\n" +
                "            \"highlight\": \"#5AD3D1\",\n" +
                "            \"label\": \"Running\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"value\": 100,\n" +
                "            \"color\": \"#FDB45C\",\n" +
                "            \"highlight\": \"#FFC870\",\n" +
                "            \"label\": \"Cycling\"\n" +
                "        }\n" +
                "]";

var testWorkoutListJson = 
                "[\n" +
                "   {\"date\": \"3/8\", \"steps\": 450},\n" +
                "   {\"date\": \"3/7\", \"steps\": 7450},\n" +
                "   {\"date\": \"3/6\", \"steps\": 6450},\n" +
                "   {\"date\": \"3/5\", \"steps\": 7450},\n" +
                "   {\"date\": \"3/4\", \"steps\": 8450},\n" +
                "   {\"date\": \"3/3\", \"steps\": 6600}\n" +
                "]";


var testWorkoutDetail =
                "[\n" +
                "   {\"date\": \"3/8\", \"steps\": 450},\n" +
                "   {\"date\": \"3/7\", \"steps\": 7450},\n" +
                "   {\"date\": \"3/6\", \"steps\": 6450},\n" +
                "   {\"date\": \"3/5\", \"steps\": 7450},\n" +
                "   {\"date\": \"3/4\", \"steps\": 8450},\n" +
                "   {\"date\": \"3/3\", \"steps\": 6600}\n" +
                "]"; 

AndroidMock.prototype.getTodayTotal = function(callback) {
    var func = window[callback];
    func(testTodayJson);
}

AndroidMock.prototype.getAllWorkouts = function(callback) {
    var func = window[callback];
    func(testWorkoutListJson);
}

AndroidMock.prototype.showDetail = function(id, page) {
    window.location.href = page + '?' + id;
}

AndroidMock.prototype.getWorkoutDetails = function(millis, callback) {
    var func = window[callback];
    func(testTodayJson);
}

var Android = new AndroidMock();
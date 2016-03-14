function AndroidMock() {
    // empty
}

AndroidMock.prototype.getWorkoutDataOn = function(timemillis) {
	return "[{\n" +
			"            \"value\": 300,\n" +
			"            \"color\":\"#F7464A\",\n" +
			"            \"highlight\": \"#FF5A5E\",\n" +
			"            \"label\": \"Walking\"\n" +
			"        }\n" +
			"]";
};

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

var testWorkoutDetail2 =
                '[\n' +
                '   {"time": 6, "steps": 456, "calories": 62,  "distance": 0.1}, \n' +
                '   {"time": 7, "steps": 56, "calories": 12,  "distance": 0.01}, \n' +
                '   {"time": 8, "steps": 106, "calories": 30,  "distance": 0.02}, \n' +
                '   {"time": 9, "steps": 223, "calories": 42,  "distance": 0.04}, \n' +
                '   {"time": 10, "steps": 46, "calories": 22,  "distance": 0.02}, \n' +
                '   {"time": 11, "steps": 56, "calories": 25,  "distance": 0.02}, \n' +
                '   {"time": 12, "steps": 1456, "calories": 100,  "distance": 0.7}, \n' +
                '   {"time": 13, "steps": 76, "calories": 32,  "distance": 0.08}, \n' +
                '   {"time": 14, "steps": 756, "calories": 55,  "distance": 0.5}, \n' +
                '   {"time": 15, "steps": 156, "calories": 54,  "distance": 0.16}, \n' +
                '   {"time": 16, "steps": 656, "calories": 81,  "distance": 0.15}, \n' +
                '   {"time": 17, "steps": 16, "calories": 2,  "distance": 0.01}, \n' +
                '   {"time": 18, "steps": 0, "calories": 0,  "distance": 0.0}, \n' +
                '   {"time": 19, "steps": 65, "calories": 20,  "distance": 0.03}, \n' +
                '   {"time": 20, "steps": 44, "calories": 10,  "distance": 0.06}, \n' +
                '   {"time": 21, "steps": 75, "calories": 25,  "distance": 0.05}, \n' +
                '   {"time": 22, "steps": 0, "calories": 0,  "distance": 0.0}, \n' +
                '   {"time": 23, "steps": 10, "calories": 1,  "distance": 0.02} \n' +
                ']\n';

AndroidMock.prototype.getTodayTotal = function(callback) {
    var func = window[callback];
    func(testTodayJson);
};

AndroidMock.prototype.getAllWorkouts = function(callback) {
    var func = window[callback];
    func(testWorkoutListJson);
};

AndroidMock.prototype.showDetail = function(id, page) {
    window.location.href = page + '?' + id;
};

AndroidMock.prototype.getWorkoutDetails = function(millis, callback) {
    var func = window[callback];
    func(testWorkoutDetail2);
};

var Android = new AndroidMock();
module.exports = function(app) {
  app.dataSources.pgIDs.automigrate('Rescuee', function(err) {
    if (err) throw err;

    app.models.Rescuee.create([{
      "userid": 1,
      "location": {
        "lat": 40.722836,
        "lng": -74.003702
      },
      "datetime": "2017-06-10T17:53:42.477Z"
    }, {
      "userid": 2,
      "location": {
        "lat": 40.811471,
        "lng": -73.946228
      },
      "datetime": "2017-06-10T09:53:42.477Z"
    }, {
      "userid": 3,
      "location": {
        "lat": 40.779372,
        "lng": -73.97438
      },
      "datetime": "2017-06-10T18:53:42.477Z"
    }, ], function(err, rescuees) {
      if (err) throw err;

      console.log('Models created: \n', rescuees);
    });
  });
};
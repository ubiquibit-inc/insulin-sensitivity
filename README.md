# insulin-sensitivity
Insulin sensitivity by blood glucose concentration in closed-loop type 1 diabetics

#### Motivation

Provide a pipeline to calculate instantaneous insulin sensitivity factor (ISF) across blood glucose ranges. 

#### Background

The [OpenAPS](https://openaps.org/) project provides tools for [\#WeAreNotWaiting](https://twitter.com/search?q=%23wearenotwaiting) people interested in providing a closed-loop solution to type-1 diabetics. Participants upload glucose and treatment data to [OpenHuman](https://github.com/danamlewis/OpenHumansDataTools).

This plot is representative of glucose and treatments over a 3 day period for an individual on standard continuous glucose monitor + insulin pump combo treatment.

![Plot 1](img/plot.png)

We note that there are [many factors](https://diatribe.org/42factors) that affect blood sugar.

#### Back-of-napkin design

Parameters: 

- Insulin 1/2-life (lambda): model with simple exponential decay, from which we can calculate *Insulin On Board* (IOB) for any point in time
- Blood glucose ranges: e.g. `32 to 384 by 32` or using quantiles 
- (Optional) time-bound (t): Only sample data across a trailing range of values
 
Features:

- IOB (calculated)
- Blood glucose value (observed)
 
Output Label:

- Point-in-time ISF estimates, with individual and population stats
 
Note: We lack data for important relevant features like sex, weight, insulin-type, etc. 

##### Inputs per individual: 

+ blood sugar data are stored in in entries\[...\].json in 5 minute increments

```json
{
    "type": "sgv", 
    "device": "share2", 
    "date": 1531871709000, 
    "sgv": 142, 
    "_id": "...", 
    "dateString": "2018-07-17T23:55:09.000Z", 
    "direction": "FortyFiveDown", 
    "": "..."       
}
```

+ insulin treatment data are stored in treatments\[...\].json at time of insulin dose delivery

```json
{
     "created_at": "2017-01-29T23:59:05Z", 
     "_id": "....", 
     "carbs": null, 
     "insulin": null, 
     "rate": 0, 
     "duration": 30, 
     "timestamp": "2017-01-29T23:59:05Z", 
     "temp": "absolute", 
     "enteredBy": "...", 
     "eventType": "Temp Basal", "absolute": 0.275
 }
```

##### Pseudo-algo

- Pre-processing
  - Convert JSON to [JSON Lines](http://jsonlines.org/)
  - Pull person identifier (personId) off of the filesystem and prepend each record
  - Store as Parquet or ORC
- Processing
  - Load *entries* from FS (orderBy dateString)
  - Load *treatments* from FS (orderBy timestamp)
  - Join by personId
  - Filter for persons with insignificant datasets
  - create Estimator to map IOB withColumn (given t and range)
  - (optional) cache or persist to disk
  - Bucketize by range - keeping in mind we may later need: {trailing data, out of range data}
  - Linear or isotonic regression across ranges (per person and population-wide)
- Output to SciPy, sciplot, ...
 
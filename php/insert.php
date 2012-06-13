<?php

	require_once('dbase.inc');
	
	$bd = mysql_connect($mysql_hostname, $mysql_user, $mysql_password) or die("Opps some thing went wrong");
	mysql_select_db($mysql_database, $bd) or die("Opps some thing went wrong");

	$deviceid = mysql_escape_string($_POST['devid']);
	$data = mysql_escape_string($_POST['data']);

	$d = json_decode(stripslashes($data));
	sortData($d, $deviceid);
	
	// print_r($d) ."<br/><br/><br/>";
	// basicdata VALUES ('','".$deviceid."','".$d[0]->sensor."')";
	// mysql_query($query) or die(mysql_error());
	// echo mysql_insert_id();

	function sortData($d, $deviceid) {
		$i=0;
		while($i<count($d)) {
			switch($d[$i]->sensor) {
				case 1:
					$query = "INSERT INTO accelerometer VALUES ('','".$deviceid."', ".$d[$i]->accelx .",". $d[$i]->accely .",". $d[$i]->accelz .",". $d[$i]->ts.");";
					mysql_query($query) or die(mysql_error());
					break;
				case 2:
					foreach($d[$i] as $key => $value) {
						if ($key != "sensor" && $key != "ts") {
							$details = explode(",", $value);
							$assid = explode(":", $details[0]);
							$capabilities = explode(":", $details[2]);
							$level = explode(":", $details[3]);
							$frequency = explode(":", $details[4]);
							$query = "INSERT INTO wifi VALUES ('','".$deviceid."',".$d[$i]->ts.",'".$assid[1]."','".$details[1]."','".$capabilities[1]."',".$level[1].",".$frequency[1].");";
							mysql_query($query) or die(mysql_error());
						}
			    	}
					break;
				case 3:
					$query = "INSERT INTO coordinates VALUES ('','".$deviceid."', ".$d[$i]->lat .",". $d[$i]->lng .",". $d[$i]->speed .",". $d[$i]->accuracy.",". $d[$i]->altitude.",". $d[$i]->ts.");";
					mysql_query($query) or die(mysql_error());
					break;
				default:
			}
			// echo $query . "<br/>";
			// mysql_query($query) or die(mysql_error());
			$i++;	
		}
	}
?>
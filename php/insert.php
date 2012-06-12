<?php

	require_once('dbase.inc');
	
	$bd = mysql_connect($mysql_hostname, $mysql_user, $mysql_password) or die("Opps some thing went wrong");
	mysql_select_db($mysql_database, $bd) or die("Opps some thing went wrong");

	$deviceid = mysql_escape_string($_GET['devid']);
	$data = mysql_escape_string($_GET['data']);

	$d = json_decode(stripslashes($data));
	sortData($d, $deviceid);
	print_r($d);
	
	 // basicdata VALUES ('','".$deviceid."','".$d[0]->sensor."')";

	// mysql_query($query) or die(mysql_error());
	// echo mysql_insert_id();


	function sortData($d, $deviceid) {
		$i=0;
		while($i<count($d)) {
			$query = "INSERT INTO ";
			switch($d[$i]->sensor) {
				case 1:
					$query .= "accelerometer VALUES ('','".$deviceid."', ".$d[$i]->accelx .",". $d[$i]->accely .",". $d[$i]->accelz .",". $d[$i]->ts.")";
					break;
				case 2:
					break;
				case 3:
					break;
				default:
			}
			// echo $query . "<br/>";
			// mysql_query($query) or die(mysql_error());
			$i++;	
		}
	}
?>
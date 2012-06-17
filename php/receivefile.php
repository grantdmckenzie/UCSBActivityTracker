<?php

 $target_path = "uploads/";
 $target_path = $target_path . basename( $_FILES['uploadedfile']['name']."-".date('U'));

 if(move_uploaded_file($_FILES['uploadedfile']['tmp_name'], $target_path)) {
	echo "The file ". basename( $_FILES['uploadedfile']['name']). " has been uploaded";
 } else {
 	$myFile = "hit.txt";
	$fh = fopen($myFile, 'w') or die("can't open file");
	fwrite($fh, "There was an error uploading the file, please try again!");
	fclose($fh);
 }

?>
get-childitem '..' -file | ForEach-Object {
    $file = $_;
    $newname = $file.DirectoryName + '\gen\' + $file.name -replace '.req', 'rand.req';

    get-content $file.fullname | out-file -FilePath $newname -Encoding utf8;

    '',"1`t$(Get-Random -Minimum 50 -max 150)",
    "2`t$(Get-Random -Minimum 50 -max 150)",
    "3`t$(Get-Random -Minimum 50 -max 150)",
    "4`t$(Get-Random -Minimum 50 -max 150)",
    "5`t$(Get-Random -Minimum 50 -max 150)" |
    Out-File -FilePath $newname -Encoding utf8 -Append
}

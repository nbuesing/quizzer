

function content(r) {

    r.warn(">> " + r.method);

    function done(res) {
        r.warn("*** status : " + res.status);

        if (406 !== res.status) {

            for (var h in res.headersOut) {
                    r.warn(">>> " + h);
                    r.headersOut[h] = res.headersOut[h];
                    //res.setHeader(h, res.headersOut[h]);
                }

            // // Axios requires Location to be the first header
            // if (res.headersOut['Location'] !== undefined) {
            //     r.warn("MAKING SURE LOCATION IS FIRST");
            //     r.headersOut['Location'] = res.headersOut['Location'];
            // } else {
            //     for (var h in res.headersOut) {
            //
            //         if (h === 'Location') {
            //             continue;
            //         }
            //
            //         r.warn(">>> " + h);
            //         r.headersOut[h] = res.headersOut[h];
            //     }
            // }

            for (var h in r.headersOut) {
                r.warn(": " + h + " : " + r.headersOut[h]);
            }

            r.return(res.status, res.responseBody);
        } else if (406 === res.status) {
            //ignore, this is from the host that isn't listening to the partitions.
            r.log("406 from host not serving.");
        }
    }

    r.subrequest('/rh1' + r.uri, { args: r.variables.args, body: r.requestBody, method: r.method}, done);
    r.subrequest('/rh2' + r.uri, { args: r.variables.args, body: r.requestBody, method: r.method}, done);
}


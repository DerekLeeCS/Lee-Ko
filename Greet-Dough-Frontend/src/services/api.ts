export const BACKEND_URL = "http://localhost:5432";

function getCurrentToken(): string {
    return localStorage.getItem("authToken") || "";
}

function setCurrentToken( token:string): void {
    localStorage.setItem("authToken", token);
}

export function logout() {
    localStorage.removeItem("authToken");
    window.location.replace("/");
}

export async function getUserID(): Promise<number> {
    let authToken = getCurrentToken();

    if (authToken === "") {
        return -1;
    }

    const res = await fetch(`${BACKEND_URL}/auth/`, {
        method: "post",
        mode: "cors",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ authToken })
    });

    if ( res.ok ) {

        let resJSON = await res.json()
            .then( body => {
                return ( JSON.parse(body).uid );
            })

        return resJSON;

    }

    else {
        // alert( "Invalid Loggin Session" );
        return -1;
    }
}

export async function register(  email:string, username:string, password:string ) {

    const res = await fetch(`${BACKEND_URL}/users/`, {
        method: "post",
        mode: "cors",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, username, password })
    });

    if ( res.ok ) {
        return 200;
    } else {
        // maybe some other code here for specific errors?
        return res.status;
    }

}

export async function login( email:string, password:string ) {

    // alert( JSON.stringify({ email, password } ) )

    const res = await fetch(`${BACKEND_URL}/login/`, {
        method: "post",
        mode: "cors",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, password })
    });

    if ( res.ok ) {

        return await res.json()
            .then(body => {
                setCurrentToken(JSON.parse(body).authToken);
                return 200;
            });

    } else {
        // maybe some other code here for specific errors?
        return res.status;
    }
}

export async function getUser( uid:number ) {
    
    const res = await fetch(`${BACKEND_URL}/users/${uid}/`, {
        headers: {
            "Content-Type": "application/json"
        },
    });

    if ( res.ok ) {

        return await res.json()
            .then(body => {
                return JSON.parse(body);
            });

    } else {
        // maybe some other code here for specific errors?
        return res.status;
    }
}

export async function getUserFeed( cuid:number, uid:number ) {

    const res = await fetch(`${BACKEND_URL}/users/${uid}/feed/`, {
        method: "get",
        mode: "cors",
        headers: {
            "Content-Type": "application/json"
        },
    });



    if( res.ok ) {
        return await res.json()
            .then(body => {
                return JSON.parse(body);
            });

    } else {
        alert("ERROR GRABBING FEED: " + res.status );
    }

}

export async function getWallet( authToken:string|null ) {

    const res = await fetch(`${BACKEND_URL}/wallet/get/${authToken}/`, {
        method: "get",
        mode: "cors",
        headers: {
            "Content-Type": "application/json"
        },
    });

    if ( res.ok ) {

        return await res.json()
            .then( body => {
                return JSON.parse(body);
            })

    } else{
        alert("ERROR: " + res.status );
    }

}

export async function addToWallet( authToken:string|null, amount:string|null ) {
    if (amount === ""){
        alert("No value inserted!");
        return;
    }

    const res = await fetch(`${BACKEND_URL}/wallet/add/${authToken}/`, {
        method: "post",
        mode: "cors",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ amount } )
    });

    if ( res.ok ) {
        return 200;
    } else {
        alert(" Error adding money to wallet ");
        return res.status;
    }
}

export async function createPost( uid:number, contents:string ) {

    // alert( JSON.stringify({ uid, contents } ) )

    const res = await fetch(`${BACKEND_URL}/posts/`, {
        method: "post",
        mode: "cors",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ uid, contents })
    });

    if ( res.ok ) {
        return 200;
    } else {
        // maybe some other code here for specific errors?
        return res.status;
    }

}

let exports = {
    register,
    login,
    getUserID,
    logout,
    getUser,
    createPost,
    getUserFeed,
    getWallet,
    addToWallet,
}

export default exports

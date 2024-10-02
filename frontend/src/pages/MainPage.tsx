import { useNavigate, Link } from 'react-router-dom';
import { Layout, Button } from '../component/common';

type LoginType = 'for_user' | 'for_franchiser';

const MainPage = () => {
  const navigate = useNavigate();

  const handleLogin = (type: LoginType) => {
    navigate('/login', { state: { loginType: type } });
  };

  const isLogin = () => {
    return !!localStorage.getItem('accessToken');
  };

  return (
    <Layout className="flex flex-col justify-center" hasBottomTab={false}>
      <main className="flex flex-col items-center justify-center px-4">
        <header className="flex flex-col items-center justify-center">
          <h1 className="mb-4 text-3xl font-bold">Olive Pay</h1>
          <img
            src="https://www.busanjarip.or.kr/img/support/topimg_01.png"
            alt="Dining illustration"
            className="w-full"
          />
        </header>

        <figure className="mt-5 items-center text-center">
          <Link to="/donation-info" className="text-base hover:text-blue-700">
            🏃‍♀️ 후원정보페이지로 바로가기
          </Link>
        </figure>
        {!isLogin() && (
          <figure className="my-10 flex w-80 flex-col items-center gap-y-4">
            <Button
              label="유저로 로그인"
              variant="primary"
              className="w-full"
              onClick={() => handleLogin('for_user')}
            />
            <Button
              label="가맹점주로 로그인"
              variant="primary"
              className="w-full"
              onClick={() => handleLogin('for_franchiser')}
            />
          </figure>
        )}
      </main>
    </Layout>
  );
};

export default MainPage;
